/**
 * TextChunkConnectionProvider.java
 *
 * Created on 26. 12. 2021, 22:23:34 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class TextChunkConnectionProvider extends ConnectionSetArtifactService
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
        return null;
    }

    @Override
    public String getCategory()
    {
        return "Relations";
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
                {
                    final var conns = extractConnections(cset, (Page) page);
                    saveConnections(input.getIri(), conns);
                    return null; // no new artifact created
                }
                else
                    throw new ServiceException("Couldn't fetch source page");
            }
            else
                throw new ServiceException("Source page not set");
        }
        else
            throw new ServiceException("Source artifact not specified or not a chunk set");
    }

    public Collection<AreaConnection> extractConnections(ChunkSet input, Page page)
    {
        Set<ContentRect> chunks = new HashSet<>(input.getTextChunks());
        RelationAnalyzer ra = new RelationAnalyzerSymmetric(page, chunks);
        ra.extractConnections();
        return ra.getConnections();
    }
    

}
