/**
 * DefaultChunkSet.java
 *
 * Created on 8. 4. 2021, 13:32:33 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * A default chunk set implementation.
 * 
 * @author burgetr
 */
public class DefaultChunkSet extends BaseArtifact implements ChunkSet
{
    private int idcnt = 1;
    
    private IRI areaTreeIri;
    private IRI pageIri;
    private Set<TextChunk> chunks;
    private AreaTopology topology;
    

    public DefaultChunkSet(IRI parentIri)
    {
        super(parentIri);
        setAreaTreeIri(parentIri);
        setTextChunks(new HashSet<>());
    }

    public DefaultChunkSet(IRI parentIri, Set<TextChunk> chunks)
    {
        super(parentIri);
        setAreaTreeIri(parentIri);
        setTextChunks(chunks);
    }

    @Override
    public IRI getArtifactType()
    {
        return SEGM.ChunkSet;
    }

    @Override
    public IRI getAreaTreeIri()
    {
        return areaTreeIri;
    }

    public void setAreaTreeIri(IRI pageIri)
    {
        this.areaTreeIri = pageIri;
    }

    @Override
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

    public void setTextChunks(Set<TextChunk> chunks)
    {
        this.chunks = chunks;
        invalidateTopology();
    }

    public void addTextChunk(TextChunk chunk)
    {
        chunks.add(chunk);
        invalidateTopology();
    }
    
    public void invalidateTopology()
    {
        topology = null;
    }
    
    public void updateTopology()
    {
        final List<ContentRect> rects = new ArrayList<>(chunks.size());
        rects.addAll(chunks);
        topology = new AreaListGridTopology(rects);
    }
    
    @Override
    public AreaTopology getTopology()
    {
        if (topology == null)
            updateTopology();
        return topology;
    }
    
    protected int getNextAreaId()
    {
        return idcnt++;
    }
    
    public void setNextAreaId(int nextId)
    {
        idcnt = nextId;
    }

    @Override
    public TextChunk createTextChunk(Rectangular r, Area sourceArea, Box sourceBox)
    {
        final DefaultTextChunk chunk = new DefaultTextChunk(r, sourceArea, sourceBox);
        chunk.setId(getNextAreaId());
        return chunk;
    }

    @Override
    public String toString()
    {
        return "ChunkSet [" + getIri() + "]";
    }

}
