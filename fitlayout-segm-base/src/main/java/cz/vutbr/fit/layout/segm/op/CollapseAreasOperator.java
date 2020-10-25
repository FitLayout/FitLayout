/**
 * CollapseAreasOperator.java
 *
 * Created on 2. 10. 2015, 16:19:32 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.segm.TreeOp;

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
        recursiveCollapseAreas(atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveCollapseAreas(root);
    }
    
    //==============================================================================

    private void recursiveCollapseAreas(Area root)
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
                recursiveCollapseAreas(root.getChildAt(i));
        }
    }
    
    private boolean canCollapse(Area area)
    {
        return (area.getChildCount() == 1 && area.getChildAt(0).isLeaf());
    }

    private void recursiveCollapseSubtree(Area src, Area dest)
    {
        for (int i = 0; i < src.getChildCount(); i++)
        {
            Area child = src.getChildAt(i);
            recursiveCollapseSubtree(child, dest);
            TreeOp.joinChild(dest, child);
        }
    }

    
}
