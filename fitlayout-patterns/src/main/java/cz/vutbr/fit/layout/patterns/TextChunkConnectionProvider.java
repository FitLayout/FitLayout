/**
 * TextChunkConnectionProvider.java
 *
 * Created on 26. 12. 2021, 22:23:34 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultConnectionSet;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.ConnectionSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class TextChunkConnectionProvider extends BaseArtifactService
{

    public TextChunkConnectionProvider()
    {
    }

    @Override
    public String getId()
    {
        return "FitLayout.TextChunkConnections";
    }

    @Override
    public String getName()
    {
        return "Text chunk connection extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts various connections between text chunks.";
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
        return SEGM.ChunkSet;
    }

    @Override
    public IRI getProduces()
    {
        return BOX.ConnectionSet;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof ChunkSet)
        {
            ChunkSet cset = (ChunkSet) input;
            if (cset.getPageIri() != null)
            {
                Artifact page = getServiceManager().getArtifactRepository().getArtifact(cset.getPageIri());
                if (page != null && page instanceof Page)
                    return extractConnections(cset, (Page) page);
                else
                    throw new ServiceException("Couldn't fetch source page");
            }
            else
                throw new ServiceException("Source page not set");
        }
        else
            throw new ServiceException("Source artifact not specified or not a chunk set");
    }

    public ConnectionSet extractConnections(ChunkSet input, Page page)
    {
        Set<ContentRect> chunks = new HashSet<>(input.getTextChunks());
        RelationAnalyzer ra = new RelationAnalyzerSymmetric(page, chunks);
        ra.extractConnections();
        
        DefaultConnectionSet ret = new DefaultConnectionSet(input.getIri());
        ret.setAreaConnections(ra.getConnections());
        ret.setPageIri(page.getIri());
        return ret;
    }
    

}
