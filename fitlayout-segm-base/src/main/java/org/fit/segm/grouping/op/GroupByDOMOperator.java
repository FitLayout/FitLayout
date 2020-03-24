/**
 * GroupByDOMOperator.java
 *
 * Created on 9. 2. 2016, 15:23:58 by burgetr
 */
package org.fit.segm.grouping.op;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

/**
 * Creates groups of areas that share the same source DOM node.
 * 
 * @author burgetr
 */
public class GroupByDOMOperator extends SuperAreaOperator
{
    
    public GroupByDOMOperator()
    {
        super(1);
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.GroupByDOM";
    }
    
    @Override
    public String getName()
    {
        return "Group by DOM nodes";
    }

    @Override
    public String getDescription()
    {
        return "..."; //TODO
    }
    
    @Override
    public void apply(AreaTree atree)
    {
        groupByDOM((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        groupByDOM((AreaImpl) root);
    }

    //==============================================================================

    @Override
    protected GroupAnalyzer createGroupAnalyzer(AreaImpl root)
    {
        return new GroupAnalyzerByDOM(root);
    }
    
    //==============================================================================

    /**
     * Takes the leaf areas and tries to join the homogeneous paragraphs.
     */
    private void groupByDOM(AreaImpl root)
    {
        if (root.getChildCount() > 1)
            findSuperAreas(root, 1);
        for (int i = 0; i < root.getChildCount(); i++)
            groupByDOM((AreaImpl) root.getChildAt(i));
    }
}
