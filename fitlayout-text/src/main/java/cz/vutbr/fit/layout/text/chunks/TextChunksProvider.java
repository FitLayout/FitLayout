/**
 * TextChunksProvider.java
 *
 * Created on 13. 5. 2021, 18:54:50 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.text.tag.FixedTaggerConfig;

/**
 * 
 * @author burgetr
 */
public class TextChunksProvider extends BaseArtifactService
{
    private TaggerConfig tagConfig;
    private boolean useWholeAreaText;
    

    public TextChunksProvider()
    {
        tagConfig = new FixedTaggerConfig();
        useWholeAreaText = false;
    }
    
    public TextChunksProvider(TaggerConfig tagConfig)
    {
        this.tagConfig = tagConfig;
        useWholeAreaText = false;
    }
    
    public TaggerConfig getTaggerConfig()
    {
        return tagConfig;
    }

    public void setTaggerConfig(TaggerConfig tagConfig)
    {
        this.tagConfig = tagConfig;
    }

    public boolean getUseWholeAreaText()
    {
        return useWholeAreaText;
    }

    public void setUseWholeAreaText(boolean useAreaText)
    {
        this.useWholeAreaText = useAreaText;
    }

    @Override
    public String getId()
    {
        return "FitLayout.TextChunks";
    }

    @Override
    public String getName()
    {
        return "Tagged text chunks extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts tagged text chunks from an area tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterBoolean("useWholeAreaText"));
        return ret;
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
        final ChunksSource csrc;
        if (useWholeAreaText)
            csrc = new AreaTextChunksSource(atree.getRoot(), tagConfig);
        else
            csrc = new TaggedChunksSource(tagConfig, atree.getRoot(), 0.1f);
        
        List<TextChunk> chunks = csrc.getTextChunks();
        DefaultChunkSet ret = new DefaultChunkSet(atree.getIri(), new HashSet<>(chunks));
        ret.setPageIri(atree.getPageIri());
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        return ret;
    }

}
