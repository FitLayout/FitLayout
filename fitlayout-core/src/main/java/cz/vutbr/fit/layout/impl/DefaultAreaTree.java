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
    private Area root;

    public DefaultAreaTree(IRI pageIri)
    {
        super(pageIri);
    }
    
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
        return new DefaultArea(r);
    }

    @Override
    public Area createArea(Box box)
    {
        return new DefaultArea(box);
    }

    @Override
    public Area createArea(List<Box> boxes)
    {
        return new DefaultArea(boxes);
    }
    
}
