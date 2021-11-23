/**
 * RDFArtifact.java
 *
 * Created on 21. 11. 2021, 12:43:20 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Set;

import org.eclipse.rdf4j.model.Statement;

/**
 * A RDF representation of an artifact.
 * 
 * @author burgetr
 */
public interface RDFArtifact
{

    /**
     * Returns additional RDF statements that do not influence the properties of the artifact
     * itself but should be preserved together with the artifact (e.g. additional annotations).
     * @return a set of statements
     */
    public Set<Statement> getAdditionalStatements();
    
    /**
     * Recomputes the possible computed values in the artifacts after the RDF representation
     * has changed (e.g. some statements have been added or removed). 
     */
    public void recompute();    
    
}
