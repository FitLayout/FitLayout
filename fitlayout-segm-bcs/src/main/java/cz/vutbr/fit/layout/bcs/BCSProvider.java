/**
 * BCSProvider.java
 *
 * Created on 4. 12. 2020, 14:20:51 by burgetr
 */
package cz.vutbr.fit.layout.bcs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.bcs.impl.AreaCreator;
import cz.vutbr.fit.layout.bcs.impl.AreaProcessor2;
import cz.vutbr.fit.layout.bcs.impl.PageArea;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class BCSProvider extends BaseArtifactService
{
    private float threshold = 0.3f;
    private boolean debug = true;

    public BCSProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.BCS";
    }

    @Override
    public String getName()
    {
        return "BCS";
    }

    @Override
    public String getDescription()
    {
        return "BCS: the Box Clustering Segmentation Algorithm";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterFloat("threshold"));
        return ret;
    }
    
    @Override
    public IRI getConsumes()
    {
        return BOX.Page;
    }

    @Override
    public IRI getProduces()
    {
        return SEGM.AreaTree;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        return createAreaTree((Page) input);
    }

    public float getThreshold()
    {
        return threshold;
    }

    public void setThreshold(float threshold)
    {
        this.threshold = threshold;
    }

    //==============================================================================
    
    private AreaTree createAreaTree(Page page)
    {
        AreaCreator c = new AreaCreator(page.getWidth(), page.getHeight());
        List<PageArea> areas = c.getAreas(page.getRoot());

        AreaProcessor2 h = new AreaProcessor2(areas, page.getWidth(), page.getHeight());
        if (threshold > 0)
            h.setThreshold(threshold);
        h.setDebug(debug);

        List<PageArea> groups = h.extractGroups(h.getAreas());
        List<PageArea> ungrouped = h.getUngrouped();

        DefaultArea root = new DefaultArea(new Rectangular(0, 0, page.getWidth() - 1, page.getHeight() - 1));
        root.setName("root");
        for (PageArea pa : groups)
        {
            Rectangular pos = new Rectangular(pa.getLeft(), pa.getTop(), pa.getRight(), pa.getBottom());
            DefaultArea child = new DefaultArea(pos); 
            root.appendChild(child);
        }
        
        //create the resulting area "tree"
        DefaultAreaTree atree = new DefaultAreaTree(page.getIri());
        atree.setParentIri(page.getIri());
        IRI atreeIri = getServiceManager().getArtifactRepository().createArtifactIri(page);
        atree.setIri(atreeIri);
        atree.setLabel(getId());
        atree.setCreator(getId());
        atree.setCreatorParams(getParamString());
        atree.setRoot(root);
        
        return atree;
    }
}
