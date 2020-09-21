/**
 * ModelBuilder.java
 *
 * Created on 21. 9. 2020, 11:20:30 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A builder that can transform an artifact to a RDF model.
 * 
 * @author burgetr
 */
public interface ModelBuilder
{
    
    /**
     * Creates a graph from an artifact.
     * @param artifact The artifact to create the model for.
     * @return the create model
     */
    public Model createGraph(Artifact artifact);

}
