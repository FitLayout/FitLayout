/**
 * RDFChunkSet.java
 *
 * Created on 10. 4. 2021, 20:33:26 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * 
 * @author burgetr
 */
public class RDFChunkSet extends DefaultChunkSet implements RDFResource
{

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

}
