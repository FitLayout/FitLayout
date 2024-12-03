/**
 * RelationAnalyzerKNN.java
 *
 * Created on 28. 11. 2024, 15:40:46 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
 * Based on the original python source by Emanuele Vivoli and Andrea Gemelli:
 * https://github.com/AILab-UniFI/GNN-TableExtraction
 * 
 * @author burgetr
 */
public class RelationAnalyzerKNN extends AreaSetRelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS = List.of(Relations.HASNEIGHBOR);

    private int k = 10; // number of nearest neighbors to consider
    private int maxDistance = 500; // maximum distance to consider (in pixels)
    
    private List<ContentRect> bboxes;
    
    private Set<AreaConnection> connections;
    
    public RelationAnalyzerKNN(Page page, Collection<ContentRect> areas)
    {
        super(page, areas);
        bboxes = new ArrayList<>(areas);
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
        buildGraphKNN();
    }
    
    //=============================================================================================
    
    public void buildGraphKNN()
    {
        int width = getPage().getWidth();
        int height = getPage().getHeight();
        
        Projections verticalProjections = new Projections(width);
        Projections horizontalProjections = new Projections(height);

        // Create projections
        for (int nodeIndex = 0; nodeIndex < bboxes.size(); nodeIndex++)
        {
            final Rect bbox = bboxes.get(nodeIndex);
            for (int hp = bbox.getX1(); hp < bbox.getX2(); hp++)
            {
                //if (hp >= width) hp = width - 1;
                if (hp >= 0 && hp < width)
                    verticalProjections.addIndex(hp, nodeIndex);
            }
            for (int vp = bbox.getY1(); vp < bbox.getY2(); vp++)
            {
                //if (vp >= height) vp = height - 1;
                if (vp >= 0 && vp < height)
                    horizontalProjections.addIndex(vp, nodeIndex);
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

        Set<Edge> edges = new HashSet<>();

        // Connecting k-nearest nodes
        for (int nodeIndex = 0; nodeIndex < bboxes.size(); nodeIndex++)
        {
            Rect nodeBbox = bboxes.get(nodeIndex);
            knn(nodeBbox, nodeIndex, verticalProjections, horizontalProjections, edges);
        }

        // Convert edges to connections
        connections = new HashSet<>();
        for (Edge edge : edges)
        {
            final float weight = (float) edge.getDistance();
            connections.add(new AreaConnection(bboxes.get(edge.getStart()), bboxes.get(edge.getEnd()), Relations.HASNEIGHBOR, weight));
        }
    }    
    
    private void knn(Rect nodeBbox, int nodeIndex, 
            Projections verticalProjections, Projections horizontalProjections, Set<Edge> edges)
    {
        List<Integer> neighbors = new ArrayList<>();
        int windowMultiplier = 2;
        final boolean wider = (nodeBbox.getX2()- nodeBbox.getX1()) > (nodeBbox.getY2() - nodeBbox.getY1());

        // finding neighbors
        while (neighbors.size() < k && windowMultiplier < 100)
        {
            List<Integer> verticalBboxes = new ArrayList<>();
            List<Integer> horizontalBboxes = new ArrayList<>();
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
                    limit0Max(nodeBbox.getX2() + hOffset, getPage().getWidth()),
                    limit0Max(nodeBbox.getY2() + vOffset, getPage().getHeight()));

            for (int i = window.getX1(); i < window.getX2(); i++)
            {
                verticalBboxes.addAll(verticalProjections.get(i));
            }
            for (int i = window.getY1(); i < window.getY2(); i++)
            {
                horizontalBboxes.addAll(horizontalProjections.get(i));
            }

            for (int v : new HashSet<>(verticalBboxes))
            {
                for (int h : new HashSet<>(horizontalBboxes))
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
            final double dist = distance(nodeBbox, bboxes.get(n));
            neighborsDistances.add(dist);
        }

        List<Integer> sortedIndices = sortIndices(neighborsDistances);
        for (int i = 0; i < Math.min(k, sortedIndices.size()); i++)
        {
            final int sdIdx = sortedIndices.get(i);
            final double dist = neighborsDistances.get(sdIdx);
            if (dist <= maxDistance)
            {
                //System.out.println(dist + " " + bboxes.get(nodeIndex) + " -> " + bboxes.get(neighbors.get(sdIdx)));
                final Edge edge = new Edge(nodeIndex, neighbors.get(sdIdx), dist);
                edges.add(edge);
            }
            else
            {
                break;
            }
        }
        //System.out.println("====");
    }

    private int limit0(int a)
    {
        if (a < 0)
            return 0;
        else
            return a;
    }

    private int limit0Max(int a, int max)
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
        final boolean left = (rectB.getX2() - rectA.getX1()) <= 0;
        final boolean bottom = (rectA.getY2() - rectB.getY1()) <= 0;
        final boolean right = (rectA.getX2() - rectB.getX1()) <= 0;
        final boolean top = (rectB.getY2() - rectA.getY1()) <= 0;
        
        // True if two rects "see" each other vertically, above or under
        final boolean vpIntersect = (rectA.getX1() <= rectB.getX2() && rectB.getX1() <= rectA.getX2());
        // True if two rects "see" each other horizontally, right or left
        final boolean hpIntersect = (rectA.getY1() <= rectB.getY2() && rectB.getY1() <= rectA.getY2());
        final boolean rectIntersect = vpIntersect && hpIntersect;
        
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

    /**
     * Sorts the indices based on the given list of double values.
     * @param list list of double values to sort indices by.
     * @return sorted list of indices.
     */
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

    /**
     * The edge is represented as a pair of node indices.
     * 
     * @author burgetr
     */
    public static class Edge
    {
        private final int node1;
        private final int node2;
        private final double dist;
        
        public Edge(int node1, int node2, double dist)
        {
            this.node1 = node1;
            this.node2 = node2;
            this.dist = dist;
        }
        
        public int getStart()
        {
            return node1;
        }

        public int getEnd()
        {
            return node2;
        }
        
        public double getDistance()
        {
            return dist;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null || getClass()!= obj.getClass())
                return false;
            Edge other = (Edge) obj;
            return node1 == other.node1 && node2 == other.node2;
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(node1, node2);
        }
        
        @Override
        public String toString()
        {
            return "(" + node1 + ", " + node2 + ")";
        }
    }
    
    /**
     * A projection of the 2D space onto a 1D axis. It's an efficient version of list of indices
     * where empty lists are represented by nulls.
     * 
     * @author burgetr
     */
    public static class Projections extends ArrayList<List<Integer>>
    {
        private static final long serialVersionUID = 1L;

        public Projections(int size)
        {
            super(size);
            for (int i = 0; i < size; i++)
                add(null);
        }
        
        public void addIndex(int index, int value)
        {
            List<Integer> list = super.get(index);
            if (list == null)
            {
                list = new ArrayList<>();
                set(index, list);
            }
            list.add(value);
        }
        
        @Override
        public List<Integer> get(int index)
        {
            final List<Integer> ret = super.get(index);
            if (ret == null)
                return Collections.emptyList();
            else
                return ret;
        }
        
    }
    
}
