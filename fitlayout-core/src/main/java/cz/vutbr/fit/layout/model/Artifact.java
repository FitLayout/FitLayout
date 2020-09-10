/**
 * Artifact.java
 *
 * Created on 23. 5. 2020, 11:57:36 by burgetr
 */
package cz.vutbr.fit.layout.model;

import org.eclipse.rdf4j.model.IRI;

/**
 * An artifact created during the page processing.
 * 
 * @author burgetr
 */
public interface Artifact
{
    
    /**
     * Gets a unique artifact ID
     * @return the ID
     */
    public IRI getIri();

    /**
     * Gets the artifact type. 
     * @return the IRI of the artifact type.
     */
    public IRI getArtifactType();
    
    /**
     * Gets the IRI of the parent artifact - the artifact this one was created from.
     * @return the parent artifact IRI or {@code null} when this is an initial artifact.
     */
    public IRI getParentIri();
    
}
