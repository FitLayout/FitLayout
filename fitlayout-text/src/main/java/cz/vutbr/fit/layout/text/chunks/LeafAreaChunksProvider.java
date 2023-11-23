/**
 * LeafAreaChunksProvider.java
 *
 * Created on 23. 11. 2023, 20:51:03 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class LeafAreaChunksProvider extends BaseArtifactService
{

    public LeafAreaChunksProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.LeafTextxtChunks";
    }

    @Override
    public String getName()
    {
        return "Leaf area text chunks extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts text chunks from all leaf areas in the tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return Collections.emptyList();
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return SEGM.ChunkSet;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof AreaTree)
            return extractChunks((AreaTree) input);
        else
            throw new ServiceException("Source artifact not specified or not an area tree");
    }
    
    //==================================================================================
    
    private ChunkSet extractChunks(AreaTree atree)
    {
        final ChunksSource csrc = new LeafAreaChunksSource(atree.getRoot());
        List<TextChunk> chunks = csrc.getTextChunks();
        DefaultChunkSet ret = new DefaultChunkSet(atree.getIri(), new HashSet<>(chunks));
        ret.setPageIri(atree.getPageIri());
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        return ret;
    }

}
