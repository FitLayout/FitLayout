/**
 * RelationAnalyzerKNN.java
 *
 * Created on 28. 11. 2024, 15:40:46 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Relation;

/**
 * Given a list of content rectangles, finds the k nearest rectangles for each one ot them.
 * 
 * @author burgetr
 */
public class RelationAnalyzerKNN extends AreaSetRelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS = List.of(Relations.HASNEIGHBOR);

    private int k = 5; // number of nearest neighbors to consider
    private int maxDistance = 500; // maximum distance to consider (in pixels)
    
    private List<ContentRect> bboxs;
    
    
    private Set<AreaConnection> connections;
    
    public RelationAnalyzerKNN(Page page, Collection<ContentRect> areas)
    {
        super(page, areas);
        bboxs = new ArrayList<>(areas);
    }

    public int getK()
    {
        return k;
    }

    public void setK(int k)
    {
        this.k = k;
    }

    @Override
    public List<Relation> getAnalyzedRelations()
    {
        return ANALYZED_RELATIONS;
    }

    @Override
    public Set<AreaConnection> getConnections()
    {
        return connections;
    }

    @Override
    protected AreaTopology createTopology(Collection<ContentRect> areas)
    {
        return null; // no topology is used in KNN
    }

    @Override
    public void extractConnections()
    {
        // TODO Auto-generated method stub
        
    }
    
    //=============================================================================================
    
    public void buildGraphKNN()
    {
        int width = getPage().getWidth();
        int height = getPage().getHeight();
        
        List<List<Integer>> verticalProjections = new ArrayList<>(width);
        List<List<Integer>> horizontalProjections = new ArrayList<>(height);

        // Initialize projections
        for (int i = 0; i < width; i++)
            verticalProjections.add(new ArrayList<>());
        for (int i = 0; i < height; i++)
            horizontalProjections.add(new ArrayList<>());

        // Create projections
        for (int nodeIndex = 0; nodeIndex < bboxs.size(); nodeIndex++)
        {
            Rect bbox = bboxs.get(nodeIndex);
            for (int hp = bbox.getX1(); hp < bbox.getX2(); hp++)
            {
                if (hp >= width) hp = width - 1;
                verticalProjections.get(hp).add(nodeIndex);
            }
            for (int vp = bbox.getY1(); vp < bbox.getY2(); vp++)
            {
                if (vp >= height) vp = height - 1;
                horizontalProjections.get(vp).add(nodeIndex);
            }
        }

        /*List<int[]> vEdges = new ArrayList<>();
        List<int[]> hEdges = new ArrayList<>();

        if ("visibility".equals(mode))
        {
            // Connecting nearest "visible" nodes
            for (int nodeIndex = 0; nodeIndex < bboxs.size(); nodeIndex++)
            {
                Rect nodeBbox = bboxs.get(nodeIndex);
                List<int[]>[] result = visibility(nodeIndex, nodeBbox);
                vEdges.addAll(result[0]);
                hEdges.addAll(result[1]);
            }
            removeVertical(vEdges, hEdges);
        }*/

        List<int[]> edges = new ArrayList<>();

        // Connecting k-nearest nodes
        for (int nodeIndex = 0; nodeIndex < bboxs.size(); nodeIndex++)
        {
            Rect nodeBbox = bboxs.get(nodeIndex);
            knn(nodeBbox, nodeIndex, verticalProjections, horizontalProjections, edges);
        }

        // Further processing of edges...
    }    
    
    private void knn(Rect nodeBbox, int nodeIndex, 
            List<List<Integer>> verticalProjections, List<List<Integer>> horizontalProjections, List<int[]> edges)
    {
        List<Integer> neighbors = new ArrayList<>();
        int windowMultiplier = 2;
        boolean wider = (nodeBbox.getX2()
                - nodeBbox.getX1()) > (nodeBbox.getY2() - nodeBbox.getY1());

        // finding neighbors
        while (neighbors.size() < k && windowMultiplier < 100)
        {
            List<Integer> verticalBboxs = new ArrayList<>();
            List<Integer> horizontalBboxs = new ArrayList<>();
            neighbors.clear();

            int hOffset, vOffset;
            if (wider)
            {
                hOffset = (int) ((nodeBbox.getX2() - nodeBbox.getX1()) * windowMultiplier / 4);
                vOffset = (int) ((nodeBbox.getY2() - nodeBbox.getY1()) * windowMultiplier);
            }
            else
            {
                hOffset = (int) ((nodeBbox.getX2() - nodeBbox.getX1()) * windowMultiplier);
                vOffset = (int) ((nodeBbox.getY2() - nodeBbox.getY1()) * windowMultiplier / 4);
            }

            Rectangular window = new Rectangular( 
                    limit0(nodeBbox.getX1() - hOffset),
                    limit0(nodeBbox.getY1() - vOffset),
                    limitMax(nodeBbox.getX2() + hOffset, getPage().getWidth()),
                    limitMax(nodeBbox.getY2() + vOffset, getPage().getHeight()));

            for (int i = window.getX1(); i < window.getX2(); i++)
            {
                verticalBboxs.addAll(verticalProjections.get(i));
            }
            for (int i = window.getY1(); i < window.getY2(); i++)
            {
                horizontalBboxs.addAll(horizontalProjections.get(i));
            }

            for (int v : new HashSet<>(verticalBboxs))
            {
                for (int h : new HashSet<>(horizontalBboxs))
                {
                    if (v == h) neighbors.add(v);
                }
            }

            windowMultiplier++;
        }

        // finding k nearest neighbors
        neighbors = new ArrayList<>(new HashSet<>(neighbors));
        neighbors.remove(Integer.valueOf(nodeIndex));

        List<Double> neighborsDistances = new ArrayList<>();
        for (int n : neighbors)
        {
            neighborsDistances.add(distance(nodeBbox, bboxs.get(n)));
        }

        List<Integer> sortedIndices = sortIndices(neighborsDistances);

        for (int i = 0; i < Math.min(k, sortedIndices.size()); i++)
        {
            int sdIdx = sortedIndices.get(i);
            if (neighborsDistances.get(sdIdx) <= maxDistance)
            {
                int[] edge = { neighbors.get(sdIdx), nodeIndex };
                if (!containsEdge(edges, edge))
                {
                    edges.add(edge);
                }
            }
            else
            {
                break;
            }
        }
    }

    private int limit0(int a)
    {
        if (a < 0)
            return 0;
        else
            return a;
    }

    private int limitMax(int a, int max)
    {
        if (a < 0)
            return 0;
        else if (a > max)
            return max;
        else
            return a;
    }

    private double distance(Rect rectA, Rect rectB) 
    {
        // Check relative position
        boolean left = (rectB.getX2() - rectA.getX1()) <= 0;
        boolean bottom = (rectA.getY2() - rectB.getY1()) <= 0;
        boolean right = (rectA.getX2() - rectB.getX1()) <= 0;
        boolean top = (rectB.getY2() - rectA.getY1()) <= 0;
        
        // True if two rects "see" each other vertically, above or under
        boolean vpIntersect = (rectA.getX1() <= rectB.getX2() && rectB.getX1() <= rectA.getX2());
        // True if two rects "see" each other horizontally, right or left
        boolean hpIntersect = (rectA.getY1() <= rectB.getY2() && rectB.getY1() <= rectA.getY2());
        boolean rectIntersect = vpIntersect && hpIntersect;
        
        if (rectIntersect)
            return 0;
        else if (top && left)
            return (int) Math.sqrt(Math.pow(rectB.getX2() - rectA.getX1(), 2) + Math.pow(rectB.getY2() - rectA.getY1(), 2));
        else if (left && bottom)
            return (int) Math.sqrt(Math.pow(rectB.getX2() - rectA.getX1(), 2) + Math.pow(rectB.getY1() - rectA.getY2(), 2));
        else if (bottom && right)
            return (int) Math.sqrt(Math.pow(rectB.getX1() - rectA.getX2(), 2) + Math.pow(rectB.getY1() - rectA.getY2(), 2));
        else if (right && top)
            return (int) Math.sqrt(Math.pow(rectB.getX1() - rectA.getX2(), 2) + Math.pow(rectB.getY2() - rectA.getY1(), 2));
        else if (left)
            return rectA.getX1() - rectB.getX2();
        else if (right)
            return rectB.getX1() - rectA.getX2();
        else if (bottom)
            return rectB.getY1() - rectA.getY2();
        else if (top)
            return rectA.getY1() - rectB.getY2();
        else
            return Double.POSITIVE_INFINITY;
    }

    private List<Integer> sortIndices(List<Double> list)
    {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
        {
            indices.add(i);
        }
        indices.sort(Comparator.comparingDouble(list::get));
        return indices;
    }

    private boolean containsEdge(List<int[]> edges, int[] edge)
    {
        for (int[] e : edges)
        {
            if (Arrays.equals(e, edge))
                return true;
        }
        return false;
    }    
    
}
