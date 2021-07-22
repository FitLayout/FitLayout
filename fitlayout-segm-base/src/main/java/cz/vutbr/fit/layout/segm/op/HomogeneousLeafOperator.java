/**
 * HomogeneousLeafOperator.java
 *
 * Created on 24. 10. 2013, 15:12:59 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;

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
        return "Detects sequences of leaf areas with a consistent style that are not visually separated"
                + " from each other and groups them to new areas.";
    }
    
    @Override
    public void apply(AreaTree atree)
    {
        findHomogeneousLeaves(atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        findHomogeneousLeaves(root);
    }

    //==============================================================================

    @Override
    protected GroupAnalyzer createGroupAnalyzer(Area root)
    {
        return new GroupAnalyzerByStyles(root, 1, true);
    }
    
    //==============================================================================

    /**
     * Takes the leaf areas and tries to join the homogeneous paragraphs.
     */
    private void findHomogeneousLeaves(Area root)
    {
        if (root.getChildCount() > 1)
            findSuperAreas(root, 1);
        for (int i = 0; i < root.getChildCount(); i++)
            findHomogeneousLeaves(root.getChildAt(i));
    }
    

}
