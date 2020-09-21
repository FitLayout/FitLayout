/**
 * LogicalAreaTree.java
 *
 * Created on 19. 3. 2015, 13:29:30 by burgetr
 */
package cz.vutbr.fit.layout.model;

import org.eclipse.rdf4j.model.IRI;

/**
 * A tree of logical areas.
 * 
 * @author burgetr
 */
public interface LogicalAreaTree extends Artifact
{
    
    /**
     * Gets the IRI of the corresponding area tree.
     * @return the area tree IRI
     */
    public IRI getAreaTreeIri();
    
    /**
     * Obtains the root node of the logical area tree.
     * 
     * @return the root node of the tree of logical areas
     */
    public LogicalArea getRoot();


}
