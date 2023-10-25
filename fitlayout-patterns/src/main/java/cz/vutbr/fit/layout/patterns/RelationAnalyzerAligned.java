/**
 * RelationAnalyzerAligned.java
 *
 * Created on 23. 10. 2023, 12:56:36 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.api.AreaUtils;
import cz.vutbr.fit.layout.impl.AreaListPixelTopology;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Relation;

/**
 * This relation analyzer just creates the above/below, onleft/onright relations on the aligned
 * areas within each sub-area.
 * @author burgetr
 */
public class RelationAnalyzerAligned implements RelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS =
            List.of(Relations.ONRIGHT, Relations.ONLEFT, 
                    Relations.BELOW, Relations.ABOVE);

    private Area rootArea;
    private Set<AreaConnection> connections;

    
    public RelationAnalyzerAligned(Area rootArea)
    {
        this.rootArea = rootArea;
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
        recursiveExtractConnections(rootArea);
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
        AreaTopology topology = new AreaListPixelTopology(rects, parent.getBounds());
        for (ContentRect src : rects)
        {
            final Rectangular pos = src.getBounds();
            addFromRegion(topology, src, 
                    new Rectangular(pos.getX2() + 1, pos.getY1() + 1, parent.getBounds().getX2(), pos.getY2() - 1),
                    Relations.ONRIGHT);
        }
    }
    
    private void addFromRegion(AreaTopology topology, ContentRect src, Rectangular region, Relation rel)
    {
        Collection<ContentRect> all = topology.findAllAreasIntersecting(region);
        for (ContentRect cand : all)
        {
            if (cand != src)
            {
                addAreaConnection(new AreaConnection(src, cand, rel, 1.0f));
                if (rel.getInverse() != null)
                    addAreaConnection(new AreaConnection(cand, src, rel.getInverse(), 1.0f));
            }
        }
    }

    protected void addAreaConnection(AreaConnection conn)
    {
        connections.add(conn);
    }

}
