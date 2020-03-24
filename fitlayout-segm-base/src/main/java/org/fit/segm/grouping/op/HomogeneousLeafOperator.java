/**
 * HomogeneousLeafOperator.java
 *
 * Created on 24. 10. 2013, 15:12:59 by burgetr
 */
package org.fit.segm.grouping.op;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

/**
 * This operator joins the homogeneous-style leaf nodes to larger artificial areas. 
 * 
 * @author burgetr
 */
public class HomogeneousLeafOperator extends SuperAreaOperator
{
    
    public HomogeneousLeafOperator()
    {
        super(10);
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.HomogeneousLeaves";
    }
    
    @Override
    public String getName()
    {
        return "Homogeneous leaves";
    }

    @Override
    public String getDescription()
    {
        return "..."; //TODO
    }
    
    @Override
    public void apply(AreaTree atree)
    {
        findHomogeneousLeaves((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        findHomogeneousLeaves((AreaImpl) root);
    }

    //==============================================================================

    @Override
    protected GroupAnalyzer createGroupAnalyzer(AreaImpl root)
    {
        return new GroupAnalyzerByStyles(root, 1, true);
    }
    
    //==============================================================================

    /**
     * Takes the leaf areas and tries to join the homogeneous paragraphs.
     */
    private void findHomogeneousLeaves(AreaImpl root)
    {
        if (root.getChildCount() > 1)
            findSuperAreas(root, 1);
        for (int i = 0; i < root.getChildCount(); i++)
            findHomogeneousLeaves((AreaImpl) root.getChildAt(i));
    }
    

}
