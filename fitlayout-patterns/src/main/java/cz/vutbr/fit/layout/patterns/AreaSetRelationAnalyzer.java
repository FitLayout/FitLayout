/**
 * PatternAnalyzer.java
 *
 * Created on 27. 2. 2016, 13:31:26 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cz.vutbr.fit.layout.impl.AreaListGridTopology;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;


/**
 * An abstract relation analyzer that operatos on a global set of areas.
 *  
 * @author burgetr
 */
public abstract class AreaSetRelationAnalyzer implements RelationAnalyzer
{
    private float minRelationWeight;
    private Page page;
    private Collection<ContentRect> areas;
    private AreaTopology topology;
    
    private Set<AreaConnection> connections;
    
    
    public AreaSetRelationAnalyzer(Page page, Collection<ContentRect> areas)
    {
        this.page = page;
        this.areas = areas;
        topology = createTopology(areas);
        connections = new HashSet<>();
        minRelationWeight = 0.1f;
    }

    /**
     * The minimal weight of the relations to be discovered.
     * @return minimal weight
     */
    public float getMinRelationWeight()
    {
        return minRelationWeight;
    }

    /**
     * Sets the minimal weight of the relations to be discovered.
     * @param minRelationWeight the minimal weight to be considered
     */
    public void setMinRelationWeight(float minRelationWeight)
    {
        this.minRelationWeight = minRelationWeight;
    }
    
    protected AreaTopology createTopology(Collection<ContentRect> areas)
    {
        return new AreaListGridTopology(areas);
    }
    
    protected void addAreaConnection(AreaConnection conn)
    {
        connections.add(conn);
    }
    
    public Set<AreaConnection> getConnections()
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
