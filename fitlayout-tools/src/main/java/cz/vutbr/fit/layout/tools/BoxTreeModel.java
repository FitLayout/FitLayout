/**
 * BoxTreeModel.java
 *
 * Created on 13. 11. 2014, 12:53:32 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import cz.vutbr.fit.layout.model.Box;

/**
 * 
 * @author burgetr
 */
public class BoxTreeModel implements TreeModel
{
    private Box root;
    
    public BoxTreeModel(Box root)
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
        return ((Box) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent)
    {
        return ((Box) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return ((Box) node).getChildCount() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        Box p = (Box) parent;
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
