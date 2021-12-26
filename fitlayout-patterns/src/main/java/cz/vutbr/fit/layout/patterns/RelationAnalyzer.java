/**
 * PatternAnalyzer.java
 *
 * Created on 27. 2. 2016, 13:31:26 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.vutbr.fit.layout.impl.AreaListGridTopology;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.patterns.model.AreaConnection;
import cz.vutbr.fit.layout.patterns.model.Relation;


/**
 * 
 * @author burgetr
 */
public abstract class RelationAnalyzer
{
    public static final float MIN_RELATION_WEIGHT = 0.1f;
    
    private Page page;
    private Collection<ContentRect> areas;
    private AreaTopology topology;
    
    private List<AreaConnection> connections;
    
    
    public RelationAnalyzer(Page page, Collection<ContentRect> areas)
    {
        this.page = page;
        this.areas = areas;
        topology = new AreaListGridTopology(areas);
        connections = new ArrayList<>();
    }

    public abstract List<Relation> getAnalyzedRelations();

    /**
     * Adds all the connections based on the evaluated relations.
     */
    public abstract void addConnections();
    
    protected void addAreaConnection(AreaConnection conn)
    {
        connections.add(conn);
    }
    
    public List<AreaConnection> getConnections()
    {
        return connections;
    }

    public Page getPage()
    {
        return page;
    }

    public Collection<ContentRect> getAreas()
    {
        return areas;
    }
    
    public AreaTopology getTopology()
    {
        return topology;
    }

}
