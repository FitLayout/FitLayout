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
     * Gets the artifact type. 
     * @return the IRI of the artifact type.
     */
    public IRI getArtifactType();
    
}
