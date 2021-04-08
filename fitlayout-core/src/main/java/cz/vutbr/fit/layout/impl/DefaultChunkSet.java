/**
 * DefaultChunkSet.java
 *
 * Created on 8. 4. 2021, 13:32:33 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * A default chunk set implementation.
 * 
 * @author burgetr
 */
public class DefaultChunkSet extends BaseArtifact implements ChunkSet
{
    private IRI pageIri;
    private Set<TextChunk> chunks;
    private AreaTopology topology;
    

    public DefaultChunkSet(IRI parentIri, Set<TextChunk> chunks)
    {
        super(parentIri);
        setPageIri(parentIri);
        setTextChunks(chunks);
    }

    @Override
    public IRI getArtifactType()
    {
        return SEGM.ChunkSet;
    }

    public IRI getPageIri()
    {
        return pageIri;
    }

    public void setPageIri(IRI pageIri)
    {
        this.pageIri = pageIri;
    }

    @Override
    public Set<TextChunk> getTextChunks()
    {
        return chunks;
    }

    protected void setTextChunks(Set<TextChunk> chunks)
    {
        final List<ContentRect> rects = new ArrayList<>(chunks.size());
        rects.addAll(chunks);
        topology = new AreaListGridTopology(rects);

    }

    @Override
    public AreaTopology getTopology()
    {
        return topology;
    }
    
}
