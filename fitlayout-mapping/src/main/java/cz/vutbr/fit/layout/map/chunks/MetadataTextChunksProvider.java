/**
 * MetadataTextChunksProvider.java
 *
 * Created on 22. 5. 2022, 21:29:50 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.text.chunks.ChunksSource;

/**
 * 
 * @author burgetr
 */
public class MetadataTextChunksProvider extends BaseArtifactService
{

    public MetadataTextChunksProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.MetadataTextChunks";
    }

    @Override
    public String getName()
    {
        return "Metadata-based chunks extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts text chunks mentioned in page metadata.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
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
        // get the annotated examples from metadata
        var repo = (RDFArtifactRepository) getServiceManager().getArtifactRepository();
        var metadataContextIri = repo.getMetadataIRI(atree.getPageIri());
        var gen = new MetadataExampleGenerator(repo, metadataContextIri, MetadataExampleGenerator::normalizeText);
        
        // setup the extractor
        ChunksSource csrc = new MetadataChunksExtractor(atree.getRoot(), gen);
        
        // create a chunk set from the chunks
        List<TextChunk> chunks = csrc.getTextChunks();
        DefaultChunkSet ret = new DefaultChunkSet(atree.getIri(), new HashSet<>(chunks));
        ret.setPageIri(atree.getPageIri());
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        return ret;
    }

}
