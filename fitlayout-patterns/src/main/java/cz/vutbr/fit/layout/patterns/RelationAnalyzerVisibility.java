/**
 * RelationAnalyzerVisibility.java
 *
 * Created on 4. 12. 2024, 16:22:21 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rect;
import cz.vutbr.fit.layout.model.Relation;

/**
 * Connects the content rectangles that are visible from each other.
 * Based on the original python source by Emanuele Vivoli and Andrea Gemelli:
 * https://github.com/AILab-UniFI/GNN-TableExtraction
 * 
 * @author burgetr
 */
public class RelationAnalyzerVisibility extends AreaSetRelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS = List.of(Relations.HASNEIGHBOR);

    private int maxDistance = 500; // maximum distance to consider (in pixels)
    
    private List<ContentRect> bboxes;
    
    private Set<AreaConnection> connections;
    
    public RelationAnalyzerVisibility(Page page, Collection<ContentRect> areas)
    {
        super(page, areas);
        bboxes = new ArrayList<>(areas);
    }

    public int getMaxDistance()
    {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance)
    {
        this.maxDistance = maxDistance;
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
        return null; // no topology is used in this analyzer
    }

    @Override
    public void extractConnections()
    {
        buildGraphVisibility();
    }
    
    //=============================================================================================
    
    public void buildGraphVisibility()
    {
        Set<Edge> edges = new HashSet<>();
        Set<Edge> hEdges = new HashSet<>();
        Set<Edge> vEdges = new HashSet<>();

        // Connecting nearest "visible" nodes
        for (int nodeIndex = 0; nodeIndex < bboxes.size(); nodeIndex++)
        {
            Rect nodeBbox = bboxes.get(nodeIndex);
            visibility(nodeBbox, nodeIndex, vEdges, hEdges);
        }
        removeVertical(vEdges, hEdges, edges);
        
        // Convert edges to connections
        connections = new HashSet<>();
        for (Edge edge : edges)
        {
            final float weight = (float) edge.getDistance();
            connections.add(new AreaConnection(bboxes.get(edge.getStart()), bboxes.get(edge.getEnd()), Relations.HASNEIGHBOR, weight));
        }
    }    
    
    public void visibility(Rect nodeBbox, int nodeIndex, Set<Edge> vEdges, Set<Edge> hEdges)
    {
        int width = getPage().getWidth();
        int height = getPage().getHeight();

        List<NeigborNode> visibilityList = new ArrayList<>(
                // the visible boxes from the current node: top, right, bottom, left 
                List.of(new NeigborNode(nodeIndex, maxDistance),
                        new NeigborNode(nodeIndex, maxDistance),
                        new NeigborNode(nodeIndex, maxDistance),
                        new NeigborNode(nodeIndex, maxDistance)));
        
        final double nCX = nodeBbox.getX2() - (nodeBbox.getX2() - nodeBbox.getX1()) / 2.0;
        final double nCY = nodeBbox.getY2() - (nodeBbox.getY2() - nodeBbox.getY1()) / 2.0;

        for (int otherIndex = 0; otherIndex < bboxes.size(); otherIndex++)
        {
            if (nodeIndex == otherIndex) continue;

            Rect otherBbox = bboxes.get(otherIndex);
            
            final double oCX = otherBbox.getX2() - (otherBbox.getX2() - otherBbox.getX1()) / 2.0;
            final double oCY = otherBbox.getY2() - (otherBbox.getY2() - otherBbox.getY1()) / 2.0;
            
            boolean top = oCY < nCY;
            boolean right = nCX < oCX;
            boolean bottom = nCY < oCY;
            boolean left = oCX < nCX;

            boolean vpIntersect = (nodeBbox.getX1() <= otherBbox.getX2()
                    && otherBbox.getX1() <= nodeBbox.getX2());
            boolean hpIntersect = (nodeBbox.getY1() <= otherBbox.getY2()
                    && otherBbox.getY1() <= nodeBbox.getY2());
            boolean rectIntersect = vpIntersect && hpIntersect;

            if (rectIntersect)
            {
                if (top)
                {
                    visibilityList.set(0, new NeigborNode(otherIndex, 0));
                }
                else if (bottom)
                {
                    visibilityList.set(2, new NeigborNode(otherIndex, 0));
                }
            }
            else if (vpIntersect)
            {
                if (top && height / 2.0 > visibilityList.get(0).getDist()
                        && visibilityList.get(0).getDist() > (nodeBbox.getY1() - otherBbox.getY2()))
                {
                    int dist = nodeBbox.getY1() - otherBbox.getY2();
                    visibilityList.set(0, new NeigborNode(otherIndex, dist));
                }
                else if (bottom && visibilityList.get(2).getDist() > (otherBbox.getY1() - nodeBbox.getY2()))
                {
                    int dist = otherBbox.getY1() - nodeBbox.getY2();
                    visibilityList.set(2, new NeigborNode(otherIndex, dist));
                }
            }
            else if (hpIntersect)
            {
                if (right && width / 2.0 > visibilityList.get(1).getDist()
                        && visibilityList.get(1).getDist() > (otherBbox.getX1()- nodeBbox.getX2()))
                {
                    int dist = otherBbox.getX1() - nodeBbox.getX2();
                    visibilityList.set(1, new NeigborNode(otherIndex, dist));
                }
                else if (left && visibilityList.get(3).getDist() > (nodeBbox.getX1() - otherBbox.getX2()))
                {
                    int dist = nodeBbox.getX1() - otherBbox.getX2();
                    visibilityList.set(3, new NeigborNode(otherIndex, dist));
                }
            }
        }

        for (int pos = 0; pos < visibilityList.size(); pos++)
        {
            NeigborNode v = visibilityList.get(pos);
            if (nodeIndex != v.getIndex())
            {
                Edge edge = new Edge(nodeIndex, v.getIndex(), v.getDist());
                Edge reverseEdge = new Edge(v.getIndex(), nodeIndex, v.getDist());
                if (pos == 0)
                { // top
                    vEdges.add(edge);
                }
                else if (pos == 3)
                { // left
                    hEdges.add(edge);
                }
                else if (pos == 2)
                { // bottom
                    vEdges.add(reverseEdge);
                }
                else if (pos == 1)
                { // right
                    hEdges.add(reverseEdge);
                }
            }
        }

    }

    public void removeVertical(Set<Edge> vEdges, Set<Edge> hEdges, Set<Edge> edges)
    {
        Set<Edge> edgesToRemove = new HashSet<>();
        for (Edge v : vEdges)
        {
            Point v1 = getCenter(bboxes.get(v.getStart()));
            Point v2 = getCenter(bboxes.get(v.getEnd()));

            for (Edge h : hEdges)
            {
                Point h1 = getCenter(bboxes.get(h.getStart()));
                Point h2 = getCenter(bboxes.get(h.getEnd()));
                if (!v1.equals(h2) && !v1.equals(h2)
                        && intersect(v1, v2, h1, h2))
                {
                    edgesToRemove.add(v);
                    break;
                }
            }
        }

        vEdges.removeAll(edgesToRemove);
        edges.addAll(vEdges);
        edges.addAll(hEdges);
    }

    private Point getCenter(Rect rect)
    {
        double centerX = rect.getX2() - (rect.getX2() - rect.getX1()) / 2.0;
        double centerY = rect.getY2() - (rect.getY2() - rect.getY1()) / 2.0;
        return new Point(centerX, centerY);
    }

    private boolean ccw(Point A, Point B, Point C)
    {
        return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
    }

    private boolean intersect(Point A, Point B, Point C, Point D)
    {
        return ccw(A, C, D) != ccw(B, C, D) && ccw(A, B, C) != ccw(A, B, D);
    }

    private static class NeigborNode
    {
        private int index;
        private int dist;

        public NeigborNode(int index, int dist)
        {
            this.index = index;
            this.dist = dist;
        }

        public int getIndex()
        {
            return index;
        }

        public int getDist()
        {
            return dist;
        }
    }

    /**
     * The edge is represented as a pair of node indices.
     * 
     * @author burgetr
     */
    private static class Edge
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
    
    private static class Point
    {
        double x, y;

        Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return Double.compare(point.x, x) == 0
                    && Double.compare(point.y, y) == 0;
        }
    }
    
}
