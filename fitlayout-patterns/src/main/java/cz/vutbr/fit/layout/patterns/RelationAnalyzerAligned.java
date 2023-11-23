/**
 * RelationAnalyzerAligned.java
 *
 * Created on 23. 10. 2023, 12:56:36 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.api.AreaUtils;
import cz.vutbr.fit.layout.impl.AreaListPixelTopology;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Relation;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * This relation analyzer just creates the above/below, onleft/onright relations on the aligned
 * areas within each sub-area (for area tree) or the text chunks (for a chunk set).
 * @author burgetr
 */
public class RelationAnalyzerAligned implements RelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS =
            List.of(Relations.ONRIGHT, Relations.ONLEFT, 
                    Relations.BELOW, Relations.ABOVE);

    private Area rootArea;
    private ChunkSet chunkSet;
    private Set<AreaConnection> connections;

    
    public RelationAnalyzerAligned(Area rootArea)
    {
        this.rootArea = rootArea;
        connections = new HashSet<>();
    }
    
    public RelationAnalyzerAligned(ChunkSet chunkSet)
    {
        this.chunkSet = chunkSet;
        connections = new HashSet<>();
    }

    @Override
    public List<Relation> getAnalyzedRelations()
    {
        return ANALYZED_RELATIONS;
    }

    @Override
    public void extractConnections()
    {
        if (rootArea!= null)
            recursiveExtractConnections(rootArea);
        else if (chunkSet!= null)
            extractConnectionsInChunkSet(chunkSet);
    }

    @Override
    public Set<AreaConnection> getConnections()
    {
        return connections;
    }
    
    //===========================================================================================
    
    private void recursiveExtractConnections(Area root)
    {
        if (root.getChildCount() > 1)
            extractConnectionsInArea(root);
        for (Area child : root.getChildren())
            recursiveExtractConnections(child);
    }
    
    private void extractConnectionsInArea(Area parent)
    {
        final List<ContentRect> rects = AreaUtils.getChildrenAsContentRects(parent);
        final Rectangular parentBounds = parent.getBounds();
        extractConnectionsFromList(rects, parentBounds);
    }

    private void extractConnectionsInChunkSet(ChunkSet chunkSet)
    {
        final List<ContentRect> rects = new ArrayList<>();
        int maxX = 0;
        int maxY = 0;
        for (TextChunk textChunk : chunkSet.getTextChunks())
        {
            final Rectangular pos = textChunk.getBounds();
            maxX = Math.max(maxX, pos.getX2());
            maxY = Math.max(maxY, pos.getY2());
            rects.add(textChunk);
        }
        extractConnectionsFromList(rects, new Rectangular(0, 0, maxX, maxY));
    }
    
    protected void extractConnectionsFromList(final List<ContentRect> rects,
            final Rectangular parentBounds)
    {
        AreaTopology topology = new AreaListPixelTopology(rects, parentBounds);
        for (ContentRect src : rects)
        {
            final Rectangular pos = src.getBounds();
            addFromRegion(topology, src, 
                    new Rectangular(pos.getX2() + 1, pos.getY1() + 1, parentBounds.getX2(), pos.getY2() - 1),
                    Relations.ONLEFT, true);
            addFromRegion(topology, src, 
                    new Rectangular(pos.getX1() + 1, pos.getY2() + 1, pos.getX2() - 1, parentBounds.getY2()),
                    Relations.ABOVE, false);
        }
    }
    
    private void addFromRegion(AreaTopology topology, ContentRect src, Rectangular region, Relation rel, boolean isHorizontal)
    {
        final Collection<ContentRect> all = topology.findAllAreasIntersecting(region);
        // find minimal distance
        int minDist = Integer.MAX_VALUE;
        for (ContentRect cand : all)
        {
            final int dist = rectDistance(cand, src, isHorizontal);
            minDist = Math.min(dist, minDist);
        }
        // use the minimal ones
        final List<ContentRect> selected = new ArrayList<>();
        if (minDist != Integer.MAX_VALUE) // at least one applicable area found
        {
            for (ContentRect cand : all)
            {
                if (rectDistance(cand, src, isHorizontal) == minDist)
                    selected.add(cand);
            }
        }
        // create relations
        for (ContentRect cand : selected)
        {
            if (cand != src)
            {
                addAreaConnection(new AreaConnection(src, cand, rel, minDist));
                if (rel.getInverse() != null)
                    addAreaConnection(new AreaConnection(cand, src, rel.getInverse(), minDist));
            }
        }
    }

    protected void addAreaConnection(AreaConnection conn)
    {
        connections.add(conn);
    }
    
    protected int rectDistance(ContentRect a1, ContentRect a2, boolean isHorizontal)
    {
        if (a1 != a2)
        {
            final Rectangular r1 = a1.getBounds();
            final Rectangular r2 = a2.getBounds();
            if (isHorizontal)
            {
                if (r1.getX2() <= r2.getX1()) // r1 - r2
                    return r2.getX1() - r1.getX2();
                else if (r2.getX2() <= r1.getX1()) // r2 - r1
                    return r1.getX1() - r2.getX2();
                else // other (overlap?)
                    return Integer.MAX_VALUE; 
            }
            else
            {
                if (r1.getY2() <= r2.getY1()) // r1 / r2
                    return r2.getY1() - r1.getY2();
                else if (r2.getY2() <= r1.getY1()) // r2 / r1
                    return r1.getY1() - r2.getY2();
                else // other (overlap?)
                    return Integer.MAX_VALUE; 
            }
        }
        else
            return Integer.MAX_VALUE;
    }

}
