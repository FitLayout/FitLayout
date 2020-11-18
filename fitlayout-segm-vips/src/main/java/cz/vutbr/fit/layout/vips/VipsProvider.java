/**
 * VipsProvider.java
 *
 * Created on 18. 11. 2020, 19:45:51 by burgetr
 */
package cz.vutbr.fit.layout.vips;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.vips.impl.Vips;

/**
 * 
 * @author burgetr
 */
public class VipsProvider extends BaseArtifactService
{

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

    //==============================================================================
    
    private AreaTree createAreaTree(Page page)
    {
        Vips vips = new Vips();
        // disable graphics output
        vips.enableGraphicsOutput(true);
        // disable output to separate folder (no necessary, it's default value is false)
        vips.enableOutputToFolder(false);
        // set permitted degree of coherence
        vips.setPredefinedDoC(8);
        // start segmentation on page
        vips.startSegmentation(page);

        AreaTree atree = new DefaultAreaTree(page.getIri());
        return atree;
    }
    
}
