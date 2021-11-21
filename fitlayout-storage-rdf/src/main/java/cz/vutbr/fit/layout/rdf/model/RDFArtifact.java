/**
 * RDFArtifact.java
 *
 * Created on 21. 11. 2021, 12:43:20 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

/**
 * A RDF representation of an artifact.
 * 
 * @author burgetr
 */
public interface RDFArtifact
{

    /**
     * Recomputes the possible computed values in the artifacts after the RDF representation
     * has changed (e.g. some statements have been added or removed). 
     */
    public void recompute();    
    
}
