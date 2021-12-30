/**
 * RDFChunkSet.java
 *
 * Created on 10. 4. 2021, 20:33:26 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * 
 * @author burgetr
 */
public class RDFChunkSet extends DefaultChunkSet implements RDFResource, RDFArtifact
{
    private Set<Statement> additionalStatements;
    private Map<IRI, RDFTextChunk> chunkIris;
    

    public RDFChunkSet(IRI parentIri)
    {
        super(parentIri);
    }

    public RDFChunkSet(IRI parentIri, IRI areaTreeIri)
    {
        super(parentIri);
        setAreaTreeIri(areaTreeIri);
    }

    public RDFChunkSet(IRI parentIri, Set<TextChunk> chunks)
    {
        super(parentIri, chunks);
    }

    public void setAdditionalStatements(Set<Statement> additionalStatements)
    {
        this.additionalStatements = additionalStatements;
    }

    @Override
    public Set<Statement> getAdditionalStatements()
    {
        return additionalStatements;
    }

    public Map<IRI, RDFTextChunk> getChunkIris()
    {
        return chunkIris;
    }

    public void setChunkIris(Map<IRI, RDFTextChunk> chunkIris)
    {
        this.chunkIris = chunkIris;
    }

    public RDFTextChunk findTextChunkByIri(IRI iri)
    {
        if (chunkIris != null)
            return chunkIris.get(iri);
        else
            return null;
    }
    
    @Override
    public void recompute()
    {
        // nothing needs to be recomputed at the moment
    }

}
