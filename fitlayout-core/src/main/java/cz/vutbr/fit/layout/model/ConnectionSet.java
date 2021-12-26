/**
 * ConnectionSet.java
 *
 * Created on 26. 12. 2021, 19:15:02 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

/**
 * A set of area connections.
 * 
 * @author burgetr
 */
public interface ConnectionSet extends Artifact
{
    
    /**
     * The IRI of the source artifacts containing the connected objects. Typically, this is
     * the same as the parent artifact.
     * @return
     */
    public IRI getSourceIri();
    
    /**
     * Gets the set of area connections.
     * @return the set of area connections/
     */
    public Set<AreaConnection> getAreaConnections();

}
