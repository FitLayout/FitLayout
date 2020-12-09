package cz.vutbr.fit.layout.bcs.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import gnu.trove.TIntProcedure;

public class AreaProcessor2
{
    private static Logger log = LoggerFactory.getLogger(AreaProcessor2.class);

    private boolean DEBUG = false;
    private final ArrayList<PageArea> areas;

    private final SpatialIndex areaTree;
    private final SpatialIndex groupTree;

    private final HashMap<Integer, PageArea> groupMap;
    private final ArrayList<PageArea> ungrouped;

    private double similarityThreshold = 0.3;

    private final int pageWidth;
    private final int pageHeight;

    private final StopWatch time;


    public AreaProcessor2(List<PageArea> areas, int width, int height) throws IOException
    {
        Collections.sort(areas, new AreaSizeComparator());
        /* Note: we store only leaf areas */
        this.areas = new ArrayList<>();
        this.areaTree = new RTree();
        this.areaTree.init(null);

        this.groupMap = new HashMap<>();
        this.groupTree = new RTree();
        this.groupTree.init(null);

        this.ungrouped = new ArrayList<>();

        this.pageHeight = width;
        this.pageWidth = height;

        this.time = new StopWatch(true);

        this.buildHierarchy(areas);
    }

    public void setThreshold(double t)
    {
        if (t < 0 || t > 1) return;

        this.similarityThreshold = t;
    }

    public double getThreshold() {
        return this.similarityThreshold;
    }

    public void setDebug(boolean d)
    {
        this.DEBUG = d;
    }

    private void buildHierarchy(List<PageArea> areas)
    {
        ArrayList<PageArea> pool = new ArrayList<>();
        ArrayList<PageArea> deleteList = new ArrayList<>();

        pool.addAll(areas);
        Collections.sort(pool, new AreaSizeComparator());
        for (PageArea area: areas) // this can't be pool, because we will modify it in the loop
        {
            for (PageArea a: deleteList)
            {
                pool.remove(a);
            }
            deleteList.clear();

            for (PageArea a: pool)
            {
                if (area == a) break;
                if (!area.contains(a)) continue;

                area.addChild(a);
                deleteList.add(a);
            }
        }
        this.extractLeafAreas(areas);
    }

    public ArrayList<PageArea> getAreas()
    {
        return this.areas;
    }

    private void extractLeafAreas(List<PageArea> areas)
    {
        this.areas.clear();
        for (PageArea a: areas)
        {
            if (a.getChildren().size() == 0)
            {
                a.setParent(null);
                a.getChildren().clear();
//                if (a.getTop() < 180)
//                    System.out.println("areas.add(new PageArea(Color.black,"+a.getLeft()+","+a.getTop()+","+a.getRight()+","+a.getBottom()+"));");
                this.areas.add(a);
                this.areaTree.add(a.getRectangle(), this.areas.size()-1);
            }
        }
    }

    public HashMap<Integer, PageArea> getGroups() throws Exception
    {
        if (this.groupMap.isEmpty())
        {
            if (!this.areas.isEmpty())
            {
                this.extractGroups(this.areas);
            }
        }

        return this.groupMap;
    }

    public List<PageArea> extractGroups(List<PageArea> areas)
    {
        ArrayList<PageAreaRelation> relations;
        ArrayList<PageArea> ret = new ArrayList<>();

        relations = this.getAreaGraph(areas);
        this.locateGroups(relations);

        this.ungrouped.clear();
        for (PageArea group: groupMap.values())
        {
            ret.add(group);
        }

        for (PageArea area: this.areas)
        {
            if (area.getParent() == null)
            {
                this.ungrouped.add(area);
            }
        }

        return ret;
    }

    private void locateGroups(ArrayList<PageAreaRelation> relations)
    {
        PageArea a, b;
        int v1, v2, vsum, groupCnt;
        PageAreaRelation relation;
        PageArea group;
        boolean area_overlap;
        AreaMatch match;
        double threshold;
        double similarity;
        int relCnt = relations.size();
        ArrayList<PageAreaRelation> mtRelations = new ArrayList<>();
        ArrayList<PageArea> mergeCandidates = new ArrayList<>();
        boolean mergeTest;

        this.time.toggle();
        while (relations.size() > 0)
        {
            do {
                relation = relations.get(0);
                relations.remove(0);
                a = relation.getA();
                b = relation.getB();
            } while (relations.size() > 0 && (a.getParent() != null || b.getParent() != null));

            if (relations.size() == 0 && a.getParent() == null && b.getParent() == null) break;

            if (DEBUG) log.debug("Picked "+relation.toString()+"\n");

            v1 = a.getAreaCount();
            v2 = b.getAreaCount();
            vsum = v1 + v2;
            groupCnt = (a.getChildren().size()==0?0:1)+(b.getChildren().size()==0?0:1);

            /* DOC: see graph of d depending on V2, there is a logarithmic dependency */
//            threshold = similarityThreshold/(Math.log10(v1+v2)+1);
            threshold = this.similarityThreshold;
            similarity = relation.getSimilarity();
            mergeTest = this.mergeTest(relation);
            if (similarity > threshold || !mergeTest)
            {
                if (similarity <= threshold && !mergeTest)
                {
                    if (DEBUG) log.debug("Merge attempt failed\n");
                    mtRelations.add(relation);
                }
                else if (similarity >= threshold)
                {
                    if (DEBUG) log.debug("Similarity comparison failed: "+similarity+" >= "+threshold+"\n");
                }
                if (relations.size() == 0 && mtRelations.size() < relCnt)
                {
                    relations.addAll(mtRelations);
                    relCnt = relations.size();
                    mtRelations.clear();
                }
                continue;
            }

            group = this.createGroup(a, b);
            mergeCandidates.clear();
            if (DEBUG) log.debug("Group: "+group.getTop()+"-"+group.getLeft()+"("+group.getWidth()+"x"+group.getHeight()+") - ("+v1+", "+v2+")\n");

            match = new AreaMatch();
            this.groupTree.intersects(group.getRectangle(), match);
            /* It will always overlap with the two areas already in the group */
            if (match.getIds().size() > groupCnt)
            {
                a.reclaimChildren();
                b.reclaimChildren();
                group.giveUpChildren();
                continue;
            }

            do {
                match = new AreaMatch();
                this.areaTree.intersects(group.getRectangle(), match);
                /* It will always overlap with the two areas already in the group */
                area_overlap = (match.getIds().size() > vsum);

                if (area_overlap)
                {
                    if (DEBUG) log.debug("overlap = true; vsum = "+vsum+"; matches = "+match.getIds().size()+"\n");
                    /* First try to include all those overlapping areas in the group */
                    if (!this.growGroup(group, match.getIds(), mergeCandidates))
                    {
                        if (DEBUG) log.debug("group grow failed\n");
                        a.reclaimChildren();
                        b.reclaimChildren();
                        group.giveUpChildren();
                        break;
                    }
                    else
                    {
                        vsum = group.getChildren().size()+mergeCandidates.size();
                        if (DEBUG) log.debug("updated vsum: " + vsum+"\n");
                    }
                }
                else
                {
                    if (DEBUG) log.debug("overlap = false; vsum = "+vsum+"; matches = "+match.getIds().size()+"\n");
                    if (mergeCandidates.size() > 0)
                    {
                        /* The group can't be expanded more by overlapping children,
                         * try to merge those areas that might be somewhere in between them */
                        if (DEBUG) log.debug("trying to merge group "+group.toString()+" and "+mergeCandidates.size()+" candidates\n");
                        if (!this.tryMerge(group, mergeCandidates))
                        {
                            if (DEBUG) log.debug("merging failed\n");
                            a.reclaimChildren();
                            b.reclaimChildren();
                            group.giveUpChildren();
                            area_overlap = true; /* Need to set this for the condition below */
                            break;
                        }
                        mergeCandidates.clear();
                        area_overlap = true; /* Need to set this for the condition below */
                    }
                }
            } while (area_overlap);

            if (!area_overlap)
            {
                /* Now we have to add children completely */
                if (DEBUG) log.debug("Final Group: "+group.getTop()+"-"+group.getLeft()+"("+group.getWidth()+"x"+group.getHeight()+")\n");
                this.transferNeighbors(a, b, group);
                this.transferRelations(a, b, group, relations);
                if (a.getId() != null) this.groupMap.remove(a.getId());
                if (b.getId() != null) this.groupMap.remove(b.getId());
                group.calculateId();
                this.groupMap.put(group.getId(), group);
                this.groupTree.delete(a.getRectangle(), 0);
                this.groupTree.delete(b.getRectangle(), 0);
                this.groupTree.add(group.getRectangle(), 0);
            }

            if (relations.size() == 0 && mtRelations.size() < relCnt)
            {
                relations.addAll(mtRelations);
                relCnt = relations.size();
                mtRelations.clear();
            }
        }
        this.time.toggle();
        System.out.println(this.time.getTotal()/1000000 + " ms");
    }

    private boolean growGroup(PageArea group, ArrayList<Integer> matches, ArrayList<PageArea> mergeCandidates)
    {
        boolean merged = true;
        PageArea area;
        ArrayList<PageArea> areas = new ArrayList<>();
        for (Integer i: matches)
        {
            areas.add(this.areas.get(i));
        }
        Collections.sort(areas, new AreaSizeComparator());
        Collections.reverse(areas);

        /* At the first pass, allow growing group only for areas that are
         * actually bordering/overlapping with different areas in the group */

        /* First identify that all the areas are either in the group or are
         * matching the condition above (overlap/borderline with other areas in the group) */
        while (merged)
        {
            merged = false;
            for (int i = 0 ; i < areas.size() ; i++)
            {
                area = areas.get(i);
                if (DEBUG) log.debug("area test for merge: "+area.toString());
                if (area.getParent() == group)
                {
                    if (DEBUG) log.debug(" (already in the group)\n");
                    areas.remove(i);
                    i--;
                    continue;
                }
                else if (area.getParent() != null)
                {
                    /* This belongs to another group - that's a show stopper */
                    if (DEBUG) log.debug(" (belongs to another group)\n");
                    return false;
                }
                else
                {
                    for (PageArea child: group.getChildren())
                    {
                        if (area.overlaps(child))
                        {
                            merged = true;
                            group.addChild(area);
                            if (DEBUG) log.debug(" (merged - overlap)\n");
                            break;
                        }
                    }

                    if (merged == true)
                    {
                        areas.remove(i);
                        i--;
                        break;
                    }
                    else
                    {
                        if (!mergeCandidates.contains(area))
                        {
                            mergeCandidates.add(area);
                        }
                        if (DEBUG) log.debug(" (not merged)\n");
                    }
                }
            }
        }

        return true;
    }

    private boolean tryMerge(PageArea group, ArrayList<PageArea> areas)
    {
        PageArea tmpArea;
        PageArea mark;
        PageArea tmpGroup = new PageArea(group);
        AreaMatch match;
        int matchCnt = 0;
        int candidateCnt = areas.size();
        boolean merge;
        ArrayList<PageArea> mergeList = new ArrayList<>();

        for (PageArea area: areas)
        {
            if (DEBUG) log.debug("candidate: "+area.toString());
            mark = new PageArea(tmpGroup);
            tmpGroup.addChild(area, true);
            if (group.contains(tmpGroup))
            {
                /* The new area doesn't make the group expand - it can be added */
                if (DEBUG) log.debug(" is within the group\n");
                group.addChild(area);
                candidateCnt--;
            }
            else
            {
                match = new AreaMatch();
                tmpGroup.resetRectangle();
                this.areaTree.intersects(tmpGroup.getRectangle(), match);
                matchCnt = match.getIds().size();
                if (matchCnt > group.getChildren().size()+candidateCnt)
                {
                    merge = false;
                    for (Integer i: match.getIds())
                    {
                        tmpArea = this.areas.get(i);
                        if (group.getChildren().contains(tmpArea)) continue;
                        if (areas.contains(tmpArea)) continue;
                        if (tmpArea.getDistanceAbsolute(mark) <= 1)
                        {
                            mergeList.add(area);
                            if (DEBUG) log.debug(" brings new adjacent box\n");
                            merge = true;
                            break;
                        }
                    }

                    if (!merge)
                    {
                        if (DEBUG) log.debug(" would bring more boxes to the group\n");
                        return false;
                    }
                }
                else
                {
                    /* Adding the area to the group extended the group but
                     * it didn't bring in any new areas */
                    if (DEBUG) log.debug(" expands the group but can be included\n");
                    group.addChild(area);
                    candidateCnt--;
                }
            }
        }

        for (PageArea a: mergeList)
        {
            if (DEBUG) log.debug("merging "+a.toString()+"\n");
            group.addChild(a);
        }

        return true;
    }

    private PageArea createGroup(PageArea a, PageArea b)
    {
        PageArea group;

        group = new PageArea(a);

        group.mergeWith(a);
        group.mergeWith(b);

        return group;
    }


    private boolean mergeTest(PageAreaRelation rel)
    {
        PageArea a, b;
        int direction;
        int aShape, bShape;

        a = rel.getA();
        b = rel.getB();
        direction = rel.getDirection();

        aShape = a.getShape();
        bShape = b.getShape();

        if (direction == PageAreaRelation.DIRECTION_HORIZONTAL)
        {
            if (aShape == bShape)
            {
                if (aShape == PageArea.SHAPE_COLUMN) return mergeTestDensity(a, b, aShape);
                else return true;
            }
            else
            {
                return this.mergeTestAlignment(a, b);
            }
        }
        else
        {
            if (aShape == bShape)
            {
                if (aShape == PageArea.SHAPE_ROW) return mergeTestDensity(a, b, aShape);
                else return true;
            }
            else
            {
                return this.mergeTestAlignment(a, b);
            }
        }
    }

    private boolean mergeTestAlignment(PageArea a, PageArea b)
    {
        PageArea tmpArea;
        AreaMatch match;
        int areaCnt;

        tmpArea = new PageArea(a);
        tmpArea.addChild(b, true);
        areaCnt = a.getAreaCount()+b.getAreaCount();

        match = new AreaMatch();
        this.areaTree.intersects(tmpArea.getRectangle(), match);
        if (match.getIds().size() <= areaCnt) return true;
        else return false;
    }

    private boolean mergeTestDensity(PageArea a, PageArea b, int shape)
    {
        double densA, densB;
        double ratio;
        int dimA, dimB;
        int cntA, cntB;

        if (shape == PageArea.SHAPE_ROW)
        {
            cntA = a.getVEdgeCount();
            dimA = a.getHeight();
            cntB = b.getVEdgeCount();
            dimB = b.getHeight();
        }
        else if (shape == PageArea.SHAPE_COLUMN)
        {
            cntA = a.getHEdgeCount();
            dimA = a.getWidth();
            cntB = b.getHEdgeCount();
            dimB = b.getWidth();
        }
        else
        {
            return false;
        }

        densA = (double)cntA / dimA;
        densB = (double)cntB / dimB;

        ratio = Math.min(densA, densB)/Math.max(densA, densB);
//        if (ratio <= (double)2/3) return true;
        if (ratio <= 0.5) return true;
        else return false;
    }

    private void transferRelations(PageArea oldGroup1, PageArea oldGroup2, PageArea newGroup, List<PageAreaRelation> relations)
    {
        int i;
        PageAreaRelation rel;
        PageAreaRelation bestRel;
        PageArea candidate;
        double tmpSimilarity;
        HashMap<PageArea, PageAreaRelation> tmpRelations = new HashMap<>();
        HashSet<PageArea> merged = new HashSet<>();

        merged.add(oldGroup1);
        merged.add(oldGroup2);
        for (PageArea child: newGroup.getChildren())
        {
            merged.add(child);
        }

        for (i = 0; i < relations.size(); i++)
        {
            rel = relations.get(i);

            if (merged.contains(rel.getA())) candidate = rel.getB();
            else if (merged.contains(rel.getB())) candidate = rel.getA();
            else candidate = null;

            if (candidate != null)
            {
                if (merged.contains(candidate))
                {
                    /* This is a corner case that both endpoints
                     * of the relation are in the new group */
                    // TODO: do some recalculations here like H/V edge count
                    if (DEBUG) log.debug("remove "+rel.toString()+" (within group)\n");
                    if (rel.getDirection() == PageAreaRelation.DIRECTION_HORIZONTAL)
                    {
                        newGroup.addHEdgeCount(rel.getCardinality());
                    }
                    else
                    {
                        newGroup.addVEdgeCount(rel.getCardinality());
                    }
                    relations.remove(i); /* Using "i" here instead of "rel" boosts perf. (6s -> 2.5s) */
                    i--; // since we removed the relation, we need to scan the one that took its place
                    continue;
                }

                /* This shouldn't happen but still ... */
                if (candidate.getParent() != null)
                {
                    candidate = candidate.getParent();
                }

                if (tmpRelations.containsKey(candidate))
                {
                    bestRel = tmpRelations.get(candidate);
                    bestRel.addCardinality(rel.getCardinality());
                    bestRel.addSimilarity(rel.getSimilarity()*rel.getCardinality());
                }
                else
                {
                    tmpSimilarity = rel.getSimilarity()*rel.getCardinality();
                    bestRel = new PageAreaRelation(newGroup, candidate, tmpSimilarity, rel.getDirection());
                    bestRel.setCardinality(rel.getCardinality());
                    tmpRelations.put(candidate, bestRel);
                }
                if (DEBUG) log.debug("remove "+rel.toString()+"\n");
                relations.remove(i); /* Using "i" here instead of "rel" boosts perf. (6s -> 2.5s) */
                i--; // since we removed the relation, we need to scan the one that took its place
            }
        }

        for (Map.Entry<PageArea, PageAreaRelation> entry : tmpRelations.entrySet())
        {
            rel = entry.getValue();
            rel.setSimilarity(rel.getSimilarity()/rel.getCardinality());
            relations.add(rel);
        }


        Collections.sort(relations, new RelationComparator());
    }

    private void transferNeighbors(PageArea oldGroup1, PageArea oldGroup2, PageArea newGroup)
    {
        PageArea area;
        PageAreaRelation rel;
        ArrayList<PageArea> delList = new ArrayList<>();
        HashMap<PageArea, Integer> recalc = new HashMap<>();
        /* children is a hash tab giving information about
         * which areas are children of the group
         * (those should not be added as neighBors of the new group)
         */
        HashMap<PageArea, Integer> children = new HashMap<>();
        for (PageArea child: newGroup.getChildren())
        {
            children.put(child, 0);
        }
        children.put(oldGroup1, 0);
        children.put(oldGroup2, 0);

        for (PageArea a: newGroup.getChildren())
        {
            delList.clear();
            /* We can also inspect children of the merged groups - they don't have any neighbors */
            for (Map.Entry<PageArea, PageAreaRelation> entry : a.getNeighbors().entrySet())
            {
                area = entry.getKey();
                rel = entry.getValue();
                delList.add(area);
                recalc.put(area, 0);
                if (!children.containsKey(area))
                {
                    newGroup.addNeighbor(area, rel.getDirection(), rel.getCardinality());
                }
            }

            for (PageArea del: delList)
            {
                del.delNeighbor(a);
            }
        }


        newGroup.calculateNeighborDistances();
        for (PageArea a: recalc.keySet())
        {
            a.calculateNeighborDistances();
        }
    }


    private ArrayList<PageAreaRelation> getAreaGraph(List<PageArea> areas)
    {
        ArrayList<PageAreaRelation> relations = new ArrayList<>();
        ArrayList<PageAreaRelation> tmpRelations = new ArrayList<>();
        int edge;
        PageArea a, b;
        Rectangle selector;
        double similarity;


        for (int i = 0; i < areas.size(); i++)
        {
            a = areas.get(i);
            /* First go right */
            /* DOC: the a.right+1 is for optimization, originally it was a.left */
            /* DOC: the selector is 1px from each side narrower so we can detect true overlaps */
            selector = new Rectangle(a.getRight()+1, a.getTop()+1, this.pageWidth, a.getBottom()-1);
            tmpRelations = this.findRelations(a, selector, PageAreaRelation.DIRECTION_HORIZONTAL);
            this.processRelations(tmpRelations, relations, true);

            /* Now go down */
            /* DOC: the a.bottom+1 is for optimization, originally it was a.top */
            /* DOC: the selector is 1px from each side narrower so we can detect true overlaps */
            selector = new Rectangle(a.getLeft()+1, a.getBottom()+1, a.getRight()-1, this.pageHeight);
            tmpRelations = this.findRelations(a, selector, PageAreaRelation.DIRECTION_VERTICAL);
            this.processRelations(tmpRelations, relations, true);

            /* DOC: Now just to be sure, go up and left, but don't add those into the global list, as we already have them */
            /* First left */
            edge = (a.getLeft()>0)?(a.getLeft()-1):0;
            selector = new Rectangle(0, a.getTop()+1, edge, a.getBottom()-1);
            tmpRelations = this.findRelations(a, selector, PageAreaRelation.DIRECTION_HORIZONTAL);
            this.processRelations(tmpRelations, relations, false);

            /* And finally up */
            edge = (a.getTop()>0)?(a.getTop()-1):0;
            selector = new Rectangle(a.getLeft()+1, 0, a.getRight()-1, edge);
            tmpRelations = this.findRelations(a, selector, PageAreaRelation.DIRECTION_VERTICAL);
            this.processRelations(tmpRelations, relations, false);
        }

        for (PageArea area: areas)
        {
            area.calculateNeighborDistances();
        }

        /* DOC: we need to compute distance now because we didn't know
         * all the absolute distances before
         */
        for (PageAreaRelation rel: relations)
        {
            a = rel.getA();
            b = rel.getB();
            rel.setAlignmentScore(rel.computeAlignmentScore());
            similarity = a.getSimilarity(b, rel.getAlignmentScore());
            rel.setSimilarity(similarity);
        }

        Collections.sort(relations, new RelationComparator());

        return relations;
    }

    private ArrayList<PageAreaRelation> findRelations(PageArea area, Rectangle selector, int direction)
    {
        AreaMatch match;
        PageArea b;
        PageAreaRelation rel;
        ArrayList<PageAreaRelation> tmpRelations = new ArrayList<>();

        match = new AreaMatch();
        this.areaTree.intersects(selector, match);
        for (Integer index: match.getIds())
        {
            b = areas.get(index);
            if (area == b) continue;
            rel = new PageAreaRelation(area, b, 1.0, direction);
            rel.setAbsoluteDistance(area.getDistanceAbsolute(b));
            tmpRelations.add(rel);
        }

        return tmpRelations;
    }

    private void processRelations(ArrayList<PageAreaRelation> batch, ArrayList<PageAreaRelation> all, boolean append)
    {
        double distMark;

        if (batch.size() > 0)
        {
            Collections.sort(batch, new AreaProximityComparator());
            /* DOC: more boxes can have the same distance */
            distMark = batch.get(0).getAbsoluteDistance();
            for (PageAreaRelation r: batch)
            {
                if (r.getAbsoluteDistance() <= distMark)
                {
                    r.getA().addNeighbor(r);
                    if (append) all.add(r);
                }
                else
                {
                    break;
                }
            }

            batch.clear();
        }
    }

    public List<PageArea> getUngrouped()
    {
        return this.ungrouped;
    }
}

class AreaMatch implements TIntProcedure
{
    private final ArrayList<Integer> ids;

    public AreaMatch()
    {
        this.ids = new ArrayList<>();
    }

    @Override
    public boolean execute(int id) 
    {
        ids.add(id);
        return true;
    }

    public ArrayList<Integer> getIds() 
    {
        return ids;
    }
};
