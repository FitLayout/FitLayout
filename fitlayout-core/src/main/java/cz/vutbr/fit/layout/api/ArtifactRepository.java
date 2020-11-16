/**
 * ArtifactRepository.java
 *
 * Created on 10.9.2020, 11:04:38 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A repository of available artifacts we are working with.
 * 
 * @author burgetr
 */
public interface ArtifactRepository
{

    /**
     * Obtains a list of all artifact IRIs in the repository.
     * @return The list of all artifacts.
     */
    public Collection<IRI> getArtifactIRIs();
    
    /**
     * Obtains an artifact from the repository.
     * @param artifactIri the artifact IRI
     * @return the artifact or {@code null} when there is no such artifact available
     */
    public Artifact getArtifact(IRI artifactIri);
    
    /**
     * Adds an artifact to the repository. If the artifact IRI, a new IRI is generated
     * using {@link #createArtifactIri(Artifact)}.
     * @param artifact the artifact to add
     */
    public void addArtifact(Artifact artifact);
    
    /**
     * Generates a unique IRI for a new artifact based on the strategy of the given repository.
     * @param artifact the artifact to generate to IRI for
     * @return The generated IRI
     */
    public IRI createArtifactIri(Artifact artifact);
    
    /**
     * Removes an artifact from the repository.
     * @param artifactIri the artifact IRI
     */
    public void removeArtifact(IRI artifactIri);
    
    /**
     * Removes all artifacts from the repository.
     */
    public void clear();
    
    /**
     * Gets an IRI encoder / decoder usable with this repository.
     * @return
     */
    public IRIDecoder getIriDecoder();
    
    /**
     * Disconnects the repository.
     */
    public void disconnect();

}
