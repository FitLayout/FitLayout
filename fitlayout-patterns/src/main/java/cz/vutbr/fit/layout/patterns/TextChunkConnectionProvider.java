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
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;
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
    private float minRelationWeight = 0.1f;
    private String method = "symmetric";
    
    private int maxDistance = 500;
    private int k = 5;
    

    public TextChunkConnectionProvider()
    {
    }

    public float getMinRelationWeight()
    {
        return minRelationWeight;
    }

    public void setMinRelationWeight(float minRelationWeight)
    {
        this.minRelationWeight = minRelationWeight;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getMaxDistance()
    {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance)
    {
        this.maxDistance = maxDistance;
    }

    public int getK()
    {
        return k;
    }

    public void setK(int k)
    {
        this.k = k;
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
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterFloat("minRelationWeight", 
                "Minimal required weight of extracted relations", -1000.0f, 1000.0f));
        ret.add(new ParameterString("method", 
                "Used analysis method {symmetric, aligned, knn, visibility}", 1, 32));
        ret.add(new ParameterInt("maxDistance",
                "Maximum distance for area connections", 1, 10000));
        ret.add(new ParameterInt("k",
                "The K parameter for KNN", 1, 50));
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
        switch (method)
        {
            case "symmetric":
                return extractConnectionsSymmetric(input, page);
            case "aligned":
                return extractConnectionsAligned(input, page);
            case "knn":
                return extractConnectionsKNN(input, page);
            case "visibility":
                return extractConnectionsVisibility(input, page);
            default:
                throw new ServiceException("Unsupported analysis method, use {symmetric, aligned, knn, visibility}");
        }
    }
    
    public Collection<AreaConnection> extractConnectionsSymmetric(ChunkSet input, Page page)
    {
        Set<ContentRect> chunks = new HashSet<>(input.getTextChunks());
        AreaSetRelationAnalyzer ra = new RelationAnalyzerSymmetric(page, chunks);
        ra.setMinRelationWeight(minRelationWeight);
        ra.extractConnections();
        return ra.getConnections();
    }
    
    public Collection<AreaConnection> extractConnectionsAligned(ChunkSet input, Page page)
    {
        RelationAnalyzerAligned ra = new RelationAnalyzerAligned(input);
        ra.extractConnections();
        return ra.getConnections();
    }
    
    public Collection<AreaConnection> extractConnectionsKNN(ChunkSet input, Page page)
    {
        Set<ContentRect> chunks = new HashSet<>(input.getTextChunks());
        RelationAnalyzerKNN ra = new RelationAnalyzerKNN(page, chunks);
        ra.setMinRelationWeight(minRelationWeight);
        ra.setK(k);
        ra.setMaxDistance(maxDistance);
        ra.extractConnections();
        return ra.getConnections();
    }

    public Collection<AreaConnection> extractConnectionsVisibility(ChunkSet input, Page page)
    {
        Set<ContentRect> chunks = new HashSet<>(input.getTextChunks());
        RelationAnalyzerVisibility ra = new RelationAnalyzerVisibility(page, chunks);
        ra.setMinRelationWeight(minRelationWeight);
        ra.setMaxDistance(maxDistance);
        ra.extractConnections();
        return ra.getConnections();
    }

}
