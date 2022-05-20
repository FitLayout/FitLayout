/**
 * Artifact.java
 *
 * Created on 23. 5. 2020, 11:57:36 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Collection;
import java.util.Date;

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
     * Sets the IRI of the artifact.
     * @param iri the new artifact IRI
     */
    public void setIri(IRI iri);
    
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
    
    /**
     * Gets the element label when it is defined.
     * @return the label or {@code null} when no label is defined
     */
    public String getLabel();
    
    /**
     * The artifact creation date.
     * @return the date/time of the artifact creation.
     */
    public Date getCreatedOn();

    /**
     * An identification of the service that created the artifact.
     * @return a service ID string
     */
    public String getCreator();
    
    /**
     * The parametres of the service used for creating the artifact.
     * @return a service parameter string
     */
    public String getCreatorParams();
    
    /**
     * Metadata provided for the entire artifact.
     * @return A metadata collection or {@code null} when no metadata is provided.
     */
    public Collection<Metadata> getMetadata();
    
}
