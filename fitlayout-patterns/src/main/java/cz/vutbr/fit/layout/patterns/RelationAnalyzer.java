/**
 * RelationAnalyzer.java
 *
 * Created on 24. 10. 2023, 20:13:10 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Relation;

/**
 * An interface of an analyzer that produces relations.
 * 
 * @author burgetr
 */
public interface RelationAnalyzer
{

    /**
     * Gets a list of relations that may be analyzed by this analyzer.
     * 
     * @return a list of relations
     */
    public List<Relation> getAnalyzedRelations();

    /**
     * Extracts all the connections based on the evaluated relations.
     */
    public abstract void extractConnections();
    
    /**
     * Gets the set of area connections previously extracted
     * by {@link #extractConnections()}.
     * 
     * @return a set of discovered area connections.
     */
    public Set<AreaConnection> getConnections();

}
