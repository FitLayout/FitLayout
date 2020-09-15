/**
 * VisualAreaTree.java
 *
 * Created on 28.6.2006, 15:10:11 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.Box.DisplayType;
import cz.vutbr.fit.layout.model.Box.Type;


/**
 * A tree of visual areas created from a box tree.
 * 
 * @author burgetr
 */
public class SegmentationAreaTree extends DefaultAreaTree
{
    private Page page;
    /** Preserve the auxiliary areas that have no visual impact */
    private boolean preserveAuxAreas;
    
    /**
     * Create a new tree of areas by the analysis of a box tree
     * @param srcpage the source box tree
     * @param preserveAuxAreas preserve the auxiliary areas that are not visually separated but
     * they are used as containers containing other ares.
     */
    public SegmentationAreaTree(Page srcPage, boolean preserveAuxAreas)
    {
        super(srcPage.getIri());
        this.page = srcPage;
        this.preserveAuxAreas = preserveAuxAreas;
        AreaImpl rootarea = new AreaImpl(0, 0, 0, 0);
        rootarea.setAreaTree(this);
        rootarea.setPageIri(srcPage.getIri());
        setRoot(rootarea);
    }
    
    /**
     * Creates the area tree skeleton - selects the visible boxes and converts
     * them to areas 
     */
    public Area findBasicAreas()
    {
        AreaImpl rootarea = new AreaImpl(0, 0, 0, 0);
        setRoot(rootarea);
        rootarea.setAreaTree(this);
        rootarea.setPageIri(getParentIri());
        for (int i = 0; i < page.getRoot().getChildCount(); i++)
        {
            Box cbox = page.getRoot().getChildAt(i);
            Area sub = new AreaImpl(cbox);
            if (sub.getWidth() > 1 || sub.getHeight() > 1)
            {
                findStandaloneAreas(page.getRoot().getChildAt(i), sub);
                rootarea.appendChild(sub);
            }
        }
        createGrids(rootarea);
        return rootarea;
    }
    
    //=================================================================================
    // factory functions producing the AreaImpl areas
    //=================================================================================
    
    @Override
    public Area createArea(Rectangular r)
    {
        return new AreaImpl(r);
    }

    @Override
    public Area createArea(Box box)
    {
        return new AreaImpl(box);
    }

    @Override
    public Area createArea(List<Box> boxes)
    {
        return new AreaImpl(boxes);
    }
    
    //=================================================================================
    
    /**
     * Goes through a box tree and tries to identify the boxes that form standalone
     * visual areas. From these boxes, new areas are created, which are added to the
     * area tree. Other boxes are ignored.
     * @param boxroot the root of the box tree
     * @param arearoot the root node of the new area tree 
     */ 
    private void findStandaloneAreas(Box boxroot, Area arearoot)
    {
        if (boxroot.isVisible())
        {
            for (int i = 0; i < boxroot.getChildCount(); i++)
            {
                Box child = boxroot.getChildAt(i);
		        if (child.isVisible())
		        {
	                if (isVisuallySeparated(child))
	                {
	                    Area newnode = new AreaImpl(child);
	                    if (newnode.getWidth() > 1 || newnode.getHeight() > 1)
	                    {
                            findStandaloneAreas(child, newnode);
	                    	arearoot.appendChild(newnode);
	                    }
	                }
	                else
	                    findStandaloneAreas(child, arearoot);
		        }
            }
        }
    }
    
    @Override
    public void updateTopologies()
    {
        createGrids((AreaImpl) getRoot());
    }

    /**
     * Goes through all the areas in the tree and creates the grids in these areas
     * @param root the root node of the tree of areas
     */
    protected void createGrids(AreaImpl root)
    {
        root.updateTopologies();
        for (int i = 0; i < root.getChildCount(); i++)
            createGrids((AreaImpl) root.getChildAt(i));
    }

    public boolean isVisuallySeparated(Box box)
    {
        //invisible boxes are not separated
        if (!box.isVisible()) 
            return false;
        //root box is visually separated
        else if (box.getParent() == null)
            return true;
        //non-empty text boxes are visually separated
        else if (box.getType() == Type.TEXT_CONTENT) 
        {
            if (box.getText().trim().isEmpty())
                return false;
            else
                return true;
        }
        //replaced boxes are visually separated
        else if (box.getType() == Type.REPLACED_CONTENT)
        {
            return true;
        }
        //list item boxes with a bullet
        else if (box.getDisplayType() == DisplayType.LIST_ITEM)
        {
            return true;
        }
        //other element boxes
        else 
        {
            if (preserveAuxAreas)
                return true;
            else
            {
                //check if separated by border -- at least one border needed
                if (box.getBorderCount() >= 1)
                    return true;
                //check the background
                else if (box.isBackgroundSeparated())
                    return true;
                return false;
            }
        }

    }
    
    //=================================================================================
    // tagging
    //=================================================================================
    
    /**
     * Obtains all the tags that are really used in the tree.
     * @return A set of used tags.
     */
    public Set<Tag> getUsedTags()
    {
        Set<Tag> ret = new HashSet<Tag>();
        recursiveGetTags(getRoot(), ret);
        return ret;
    }
    
    private void recursiveGetTags(Area root, Set<Tag> dest)
    {
        dest.addAll(root.getTags().keySet());
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveGetTags(root.getChildAt(i), dest);
    }
    
}
