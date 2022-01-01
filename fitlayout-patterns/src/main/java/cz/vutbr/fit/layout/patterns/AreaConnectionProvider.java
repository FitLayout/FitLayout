/**
 * AreaConnectionProvider.java
 *
 * Created on 26. 12. 2021, 22:18:40 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultConnectionSet;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ConnectionSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class AreaConnectionProvider extends BaseArtifactService
{

    public AreaConnectionProvider()
    {
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
        return BOX.ConnectionSet;
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
                    return extractConnections(atree, (Page) page);
                else
                    throw new ServiceException("Couldn't fetch source page");
            }
            else
                throw new ServiceException("Source page not set");
        }
        else
            throw new ServiceException("Source artifact not specified or not an area tree");
    }

    public ConnectionSet extractConnections(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzer ra = new RelationAnalyzerSymmetric(page, leafAreas);
        ra.extractConnections();
        
        DefaultConnectionSet ret = new DefaultConnectionSet(input.getIri());
        ret.setAreaConnections(ra.getConnections());
        ret.setPageIri(page.getIri());
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        return ret;
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
