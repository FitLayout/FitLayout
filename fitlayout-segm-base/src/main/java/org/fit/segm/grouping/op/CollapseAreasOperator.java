/**
 * CollapseAreasOperator.java
 *
 * Created on 2. 10. 2015, 16:19:32 by burgetr
 */
package org.fit.segm.grouping.op;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

/**
 * This operator collapses the areas having only one (leaf) child.
 * 
 * @author burgetr
 */
public class CollapseAreasOperator extends BaseOperator
{
    
    public CollapseAreasOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.CollapseAreas";
    }
    
    @Override
    public String getName()
    {
        return "Collapse areas";
    }

    @Override
    public String getDescription()
    {
        return "Collapses the areas having only one (leaf) child.";
    }

    @Override
    public String getCategory()
    {
        return "restructure";
    }

    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        recursiveCollapseAreas((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveCollapseAreas((AreaImpl) root);
    }
    
    //==============================================================================

    private void recursiveCollapseAreas(AreaImpl root)
    {
        if (canCollapse(root))
        {
            //System.out.println("Collapsing: " + root);
            recursiveCollapseSubtree(root, root);
            root.removeAllChildren();
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                recursiveCollapseAreas((AreaImpl) root.getChildAt(i));
        }
    }
    
    private boolean canCollapse(AreaImpl area)
    {
        return (area.getChildCount() == 1 && area.getChildAt(0).isLeaf());
    }

    private void recursiveCollapseSubtree(AreaImpl src, AreaImpl dest)
    {
        for (int i = 0; i < src.getChildCount(); i++)
        {
            AreaImpl child = (AreaImpl) src.getChildAt(i);
            recursiveCollapseSubtree(child, dest);
            dest.joinChild(child);
        }
    }

    
}
