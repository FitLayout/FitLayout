/**
 * LogicalTreeModel.java
 *
 * Created on 19. 3. 2015, 21:49:29 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import cz.vutbr.fit.layout.model.LogicalArea;

/**
 * 
 * @author burgetr
 */
public class LogicalTreeModel implements TreeModel
{
    private LogicalArea root;

    public LogicalTreeModel(LogicalArea root)
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
        return ((LogicalArea) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent)
    {
        return ((LogicalArea) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return ((LogicalArea) node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        LogicalArea p = (LogicalArea) parent;
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
