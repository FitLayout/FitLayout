/**
 * FlattenTreeOperator.java
 *
 * Created on 9. 11. 2016, 15:16:28 by burgetr
 */
package org.fit.segm.grouping.op;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;

/**
 * This operator flattens the tree: only the root area and the leaf areas are preserved.
 * @author burgetr
 */
public class FlattenTreeOperator extends BaseOperator
{
    //private static Logger log = LoggerFactory.getLogger(FlattenTreeOperator.class);
    
    public FlattenTreeOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.FlattenTree";
    }
    
    @Override
    public String getName()
    {
        return "Flatten tree";
    }

    @Override
    public String getDescription()
    {
        return "This operator flattens the tree: only the root area and the leaf areas are preserved";
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
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        List<Area> addList = new LinkedList<Area>();
        List<Area> removeList = new LinkedList<Area>();
        scanAreas(root, addList, removeList);
        for (Area area : addList)
            root.appendChild(area);
        removeAreas(root, removeList);
    }
    
    //==============================================================================
    
    private void scanAreas(Area root, List<Area> addList, List<Area> removeList)
    {
        if (root.getParent() != null)
        {
            if (root.isLeaf())
                addList.add(root);
            else
                removeList.add(root);
        }
        for (Area child : root.getChildren())
            scanAreas(child, addList, removeList);
    }

    private void removeAreas(Area root, List<Area> toRemove)
    {
        List<Area> curChildren = new ArrayList<>(root.getChildren());
        //call recursively on children
        for (Area child : curChildren)
            removeAreas(child, toRemove);
        //remove the selected child nodes
        for (Area child : curChildren)
        {
            if (toRemove.contains(child))
                root.removeChild(child);
        }
        root.updateTopologies();
    }
    
}
