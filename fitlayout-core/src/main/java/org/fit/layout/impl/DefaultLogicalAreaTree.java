/**
 * DefaultLogicalAreaTree.java
 *
 * Created on 18. 1. 2016, 13:41:17 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.LogicalAreaTree;

/**
 * Default implementation of the logical area tree.
 * 
 * @author burgetr
 */
public class DefaultLogicalAreaTree implements LogicalAreaTree
{
    private AreaTree atree;
    private LogicalArea root;
    
    public DefaultLogicalAreaTree(AreaTree atree)
    {
        this.atree = atree;
    }
    
    @Override
    public AreaTree getAreaTree()
    {
        return atree;
    }

    @Override
    public LogicalArea getRoot()
    {
        return root;
    }

    public void setRoot(LogicalArea root)
    {
        this.root = root;
    }
}
