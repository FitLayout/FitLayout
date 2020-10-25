/**
 * DefaultAreaTree.java
 *
 * Created on 16. 1. 2016, 20:50:33 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Default Page implementation.
 *  
 * @author burgetr
 */
public class DefaultAreaTree extends BaseArtifact implements AreaTree
{
    private IRI pageIri;
    private Area root;

    /**
     * Creates an area tree from a page.
     * @param pageIri the IRI of the source page
     */
    public DefaultAreaTree(IRI pageIri)
    {
        super(pageIri);
        setPageIri(pageIri);
    }
    
    /**
     * Creates an area tree from another artifact
     * @param parentIri the parent artifact IRI
     * @param pageIri the associated page IRI
     */
    public DefaultAreaTree(IRI parentIri, IRI pageIri)
    {
        super(parentIri);
        setPageIri(pageIri);
    }
    
    /**
     * Creates a copy of an area tree.
     * @param src the source area tree
     */
    public DefaultAreaTree(AreaTree src)
    {
        super(src.getParentIri());
        root = src.getRoot();
    }
    
    @Override
    public IRI getArtifactType()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getPageIri()
    {
        return pageIri;
    }
    
    public void setPageIri(IRI pageIri)
    {
        this.pageIri = pageIri;
    }

    @Override
    public Area getRoot()
    {
        return root;
    }

    public void setRoot(Area root)
    {
        this.root = root;
    }

    @Override
    public void updateTopologies()
    {
        //no default implementation
    }

    @Override
    public String toString()
    {
        return "AreaTree [" + getIri() + "]";
    }

    //=================================================================================
    // node search
    //=================================================================================
    
    public Area getAreaAt(int x, int y)
    {
        return recursiveGetAreaAt(root, x, y);
    }
    
    private Area recursiveGetAreaAt(Area root, int x, int y)
    {
        if (root.getBounds().contains(x, y))
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                Area ret = recursiveGetAreaAt(root.getChildAt(i), x, y);
                if (ret != null)
                    return ret;
            }
            return root;
        }
        else
            return null;
    }
    
    @Override
    public List<Area> getAreasAt(int x, int y)
    {
        List<Area> ret = new ArrayList<Area>();
        recursiveGetAreasAt(root, x, y, ret);
        return ret;
    }

    private void recursiveGetAreasAt(Area root, int x, int y, List<Area> dest)
    {
        if (root.getBounds().contains(x, y))
        {
            dest.add(root);
            for (int i = 0; i < root.getChildCount(); i++)
                recursiveGetAreasAt(root.getChildAt(i), x, y, dest);
        }
    }
    
    public Area getAreaByName(String name)
    {
        return recursiveGetAreaByName(root, name);
    }
    
    private Area recursiveGetAreaByName(Area root, String name)
    {
        if (root.toString().indexOf(name) != -1) //TODO ???
            return root;
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                Area ret = recursiveGetAreaByName(root.getChildAt(i), name);
                if (ret != null)
                    return ret;
            }
            return null;
        }
    }

    //=================================================================================
    // factory functions
    //=================================================================================
    
    @Override
    public Area createArea(Rectangular r)
    {
        DefaultArea ret = new DefaultArea(r);
        ret.setPageIri(pageIri);
        return ret;
    }

    @Override
    public Area createArea(Box box)
    {
        DefaultArea ret = new DefaultArea(box);
        ret.setPageIri(pageIri);
        return ret;
    }

    @Override
    public Area createArea(Area other)
    {
        DefaultArea ret = new DefaultArea(other);
        ret.setPageIri(pageIri);
        return ret;
    }

    @Override
    public Area createArea(List<Box> boxes)
    {
        DefaultArea ret = new DefaultArea(boxes);
        ret.setPageIri(pageIri);
        return ret;
    }
    
}
