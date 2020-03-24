/**
 * AreaTreeModel.java
 *
 * Created on 13. 11. 2014, 12:59:26 by burgetr
 */
package org.fit.layout.tools;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.fit.layout.model.Area;

/**
 * 
 * @author burgetr
 */
public class AreaTreeModel implements TreeModel
{
    private Area root;

    public AreaTreeModel(Area root)
    {
        this.root = root;
    }

    @Override
    public Object getRoot()
    {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index)
    {
        return ((Area) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent)
    {
        return ((Area) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return ((Area) node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        Area p = (Area) parent;
        for (int i = 0; i < p.getChildCount(); i++)
        {
            if (p.getChildAt(i) == child)
                return i;
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l)
    {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {

    }

}
