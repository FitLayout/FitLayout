/**
 * ModelLoader.java
 *
 * Created on 30. 9. 2020, 22:22:42 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A loader that can load an artifact instance from a storage. 
 * 
 * @author burgetr
 */
public interface ModelLoader
{
    
    /**
     * Loads an artifact from an RDF repository.
     * @param artifactIri
     * @param artifactRepo
     * @throws RepositoryException
     * @return
     */
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo)
        throws RepositoryException;

}
