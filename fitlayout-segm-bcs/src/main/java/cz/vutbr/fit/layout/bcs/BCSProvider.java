/**
 * BCSProvider.java
 *
 * Created on 4. 12. 2020, 14:20:51 by burgetr
 */
package cz.vutbr.fit.layout.bcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.bcs.impl.AreaCreator;
import cz.vutbr.fit.layout.bcs.impl.AreaProcessor2;
import cz.vutbr.fit.layout.bcs.impl.ImageOutput;
import cz.vutbr.fit.layout.bcs.impl.PageArea;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.model.Area;
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
        if (input != null && input instanceof Page)
        {
            AreaTree atree = createAreaTree((Page) input);
            IRI atreeIri = getServiceManager().getArtifactRepository().createArtifactIri(atree);
            atree.setIri(atreeIri);
            return atree;
        }
        else
            throw new ServiceException("Source artifact not specified or not a page");
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
    
    public AreaTree createAreaTree(Page page)
    {
        AreaCreator c = new AreaCreator(page.getWidth(), page.getHeight());
        List<PageArea> areas = c.getAreas(page.getRoot());
        
        final boolean graphOutput = (System.getProperty("fitlayout.bcs.debug") != null);
        if (graphOutput)
        {
            ImageOutput imgout = new ImageOutput(areas, page.getWidth(), page.getHeight());
            imgout.save("areas.png");
        }

        AreaProcessor2 h = new AreaProcessor2(areas, page.getWidth(), page.getHeight());
        if (threshold > 0)
            h.setThreshold(threshold);
        h.setDebug(debug);

        List<PageArea> groups = h.extractGroups(h.getAreas());
        List<PageArea> ungrouped = h.getUngrouped();
        List<PageArea> all = unifyAreas(groups, ungrouped);

        //create the resulting area "tree"
        DefaultAreaTree atree = new DefaultAreaTree(page.getIri());
        atree.setParentIri(page.getIri());
        atree.setLabel(getId());
        atree.setCreator(getId());
        atree.setCreatorParams(getParamString());

        //create an artificial root area
        Area root = atree.createArea(new Rectangular(0, 0, page.getWidth() - 1, page.getHeight() - 1));
        root.setName("root");
        atree.setRoot(root);
        
        //append the groups as the child nodes of the tree
        appendGroups(atree, root, all);
        
        return atree;
    }

    private List<PageArea> unifyAreas(List<PageArea> groups, List<PageArea> ungrouped)
    {
        List<PageArea> all = new ArrayList<>(groups.size() + ungrouped.size());
        all.addAll(groups);
        all.addAll(ungrouped);
        Collections.sort(all, new Comparator<PageArea>() {
            @Override
            public int compare(PageArea a1, PageArea a2)
            {
                if (a1.getTop() == a2.getTop())
                    return a1.getLeft() - a2.getLeft();
                else
                    return a1.getTop() - a2.getTop();
            }
        });
        return all;
    }
    
    
    private void appendGroups(AreaTree atree, Area root, List<PageArea> groups)
    {
        for (PageArea pa : groups)
        {
            final Rectangular pos = new Rectangular(pa.getLeft(), pa.getTop(), pa.getRight(), pa.getBottom());
            final Area child = atree.createArea(pos); 
            root.appendChild(child);
        }
    }
}
