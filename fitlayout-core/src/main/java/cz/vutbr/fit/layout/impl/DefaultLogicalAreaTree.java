/**
 * DefaultLogicalAreaTree.java
 *
 * Created on 18. 1. 2016, 13:41:17 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.ontology.SEGM;

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
    public IRI getArtifactType()
    {
        return SEGM.LogicalAreaTree;
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
