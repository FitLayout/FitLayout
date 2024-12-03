/**
 * AreaConnectionProvider.java
 *
 * Created on 26. 12. 2021, 22:18:40 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class AreaConnectionProvider extends ConnectionSetArtifactService
{
    private float minRelationWeight = 0.1f;
    private String method = "symmetric";
    

    public AreaConnectionProvider()
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

    @Override
    public String getId()
    {
        return "FitLayout.AreaConnections";
    }

    @Override
    public String getName()
    {
        return "Area connection extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts various connections between visual areas.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterFloat("minRelationWeight", 
                "Minimal required weight of extracted relations", -1000.0f, 1000.0f));
        ret.add(new ParameterString("method", 
                "Used analysis method {symmetric, aligned}", 1, 32));
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
        if (input != null && input instanceof AreaTree)
        {
            AreaTree atree = (AreaTree) input;
            if (atree.getPageIri() != null)
            {
                Artifact page = getServiceManager().getArtifactRepository().getArtifact(atree.getPageIri());
                if (page != null && page instanceof Page)
                {
                    final var conns = extractConnections(atree, (Page) page);
                    saveConnections(input.getIri(), conns);
                    return null;
                }
                else
                    throw new ServiceException("Couldn't fetch source page");
            }
            else
                throw new ServiceException("Source page not set");
        }
        else
            throw new ServiceException("Source artifact not specified or not an area tree");
    }

    public Collection<AreaConnection> extractConnections(AreaTree input, Page page)
    {
        switch (method)
        {
            case "symmetric":
                return extractConnectionsSymmetric(input, page);
            case "aligned":
                return extractConnectionsAligned(input, page);
            case "knn":
                return extractConnectionsKNN(input, page);
            default:
                throw new ServiceException("Unsupported analysis method, use {symmetric, aligned, knn}");
        }
    }
    
    public Collection<AreaConnection> extractConnectionsSymmetric(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzerSymmetric ra = new RelationAnalyzerSymmetric(page, leafAreas);
        ra.setMinRelationWeight(minRelationWeight);
        ra.extractConnections();
        return ra.getConnections();
    }

    public Collection<AreaConnection> extractConnectionsAligned(AreaTree input, Page page)
    {
        RelationAnalyzerAligned ra = new RelationAnalyzerAligned(input.getRoot());
        //ra.setMinRelationWeight(minRelationWeight); // TODO
        ra.extractConnections();
        return ra.getConnections();
    }
    
    public Collection<AreaConnection> extractConnectionsKNN(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzerKNN ra = new RelationAnalyzerKNN(page, leafAreas);
        ra.setMinRelationWeight(minRelationWeight);
        ra.extractConnections();
        return ra.getConnections();
    }
    
    private void findLeafAreas(Area root, List<ContentRect> areas)
    {
        if (root.isLeaf())
            areas.add(root);
        else
        {
            for (Area child : root.getChildren())
            {
                findLeafAreas(child, areas);
            }
        }
    }
    
}
