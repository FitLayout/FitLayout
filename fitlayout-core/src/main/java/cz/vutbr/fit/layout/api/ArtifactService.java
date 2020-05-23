/**
 * ArtifactService.java
 *
 * Created on 23. 5. 2020, 12:20:07 by burgetr
 */
package cz.vutbr.fit.layout.api;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A service that creates an artifact from another artifact.
 * 
 * @author burgetr
 */
public interface ArtifactService extends Service
{

    /**
     * The artifact type consumed.
     * 
     * @return the artifact type IRI or {@code null} when nothing is consumed (source only).
     */
    public IRI getConsumes();
    
    /**
     * The artifact type produced.
     * 
     * @return the artifact type IRI or {@code null} when nothing is produced (consumer only).
     */
    public IRI getProduces();
    
    /**
     * Consumes an input artifact and produces an output artifact.
     * @param source The input artifact or {@code null} when nothing is consumed
     * @return the output artifact {@code null} when nothing is produced
     */
    public Artifact process(Artifact input);
    
}
