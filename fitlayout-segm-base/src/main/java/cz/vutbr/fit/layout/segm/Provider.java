/**
 * Provider.java
 *
 * Created on 15. 1. 2015, 15:03:22 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * An artifact service provider for transforming the box tree to the area tree. It implements a basic
 * algorithm that only selects the visually separated boxes as the areas.
 * 
 * @author burgetr
 */
public class Provider extends BaseArtifactService
{
    /** Preserve the auxiliary areas that have no visual impact */
    private boolean preserveAuxAreas;
    
    
    public Provider()
    {
        this.preserveAuxAreas = false;
    }
    
    public Provider(boolean presereAuxAreas)
    {
        this.preserveAuxAreas = presereAuxAreas;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Grouping";
    }

    @Override
    public String getName()
    {
        return "FitLayout grouping segmentation algorithm";
    }

    @Override
    public String getDescription()
    {
        return "A configurable bottom-up segmentation algorithm";
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
    public Artifact process(Artifact input)
    {
        return createAreaTree((Page) input);
    }

    public AreaTree createAreaTree(Page page)
    {
        SegmentationAreaTree atree = new SegmentationAreaTree(page, preserveAuxAreas);
        atree.findBasicAreas();
        atree.setParentIri(page.getIri());
        IRI atreeIri = getServiceManager().getArtifactRepository().createArtifactIri(page);
        atree.setIri(atreeIri);
        return atree; 
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterBoolean("preserveAuxAreas"));
        return ret;
    }
    
    public boolean getPreserveAuxAreas()
    {
        return preserveAuxAreas;
    }

    public void setPreserveAuxAreas(boolean preserveAuxAreas)
    {
        this.preserveAuxAreas = preserveAuxAreas;
    }

}
