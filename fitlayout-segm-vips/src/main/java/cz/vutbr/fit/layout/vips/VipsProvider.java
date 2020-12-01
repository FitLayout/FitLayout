/**
 * VipsProvider.java
 *
 * Created on 18. 11. 2020, 19:45:51 by burgetr
 */
package cz.vutbr.fit.layout.vips;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.vips.impl.Vips;
import cz.vutbr.fit.layout.vips.impl.VipsTreeBuilder;

/**
 * 
 * @author burgetr
 */
public class VipsProvider extends BaseArtifactService
{
    private int pDoC = 10;

    public VipsProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.VIPS";
    }

    @Override
    public String getName()
    {
        return "VIPS";
    }

    @Override
    public String getDescription()
    {
        return "VIPS: a VIsion based Page Segmentation Algorithm";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterInt("pDoC", 1, 11));
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

    public int getPDoC()
    {
        return pDoC;
    }

    public void setPDoC(int pDoC)
    {
        this.pDoC = pDoC;
    }

    //==============================================================================
    
    private AreaTree createAreaTree(Page page)
    {
        DefaultAreaTree atree = new DefaultAreaTree(page.getIri());
        atree.setParentIri(page.getIri());
        IRI atreeIri = getServiceManager().getArtifactRepository().createArtifactIri(page);
        atree.setIri(atreeIri);
        atree.setCreator(getId());
        atree.setCreatorParams(getParamString());
        
        Vips vips = new Vips();
        // disable graphics output
        vips.enableGraphicsOutput(true);
        // disable output to separate folder (no necessary, it's default value is false)
        vips.enableOutputToFolder(false);
        // set permitted degree of coherence
        vips.setPredefinedDoC(pDoC);
        // start segmentation on page
        vips.startSegmentation(page);
        // build the tree
        VipsTreeBuilder builder = vips.getTreeBuilder();
        Area root = builder.buildAreaTree(vips.getVisualStructure());
        atree.setRoot(root);

        return atree; 
    }
    
}
