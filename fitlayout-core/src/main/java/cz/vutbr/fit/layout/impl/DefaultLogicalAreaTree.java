/**
 * DefaultLogicalAreaTree.java
 *
 * Created on 18. 1. 2016, 13:41:17 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Default implementation of the logical area tree.
 * 
 * @author burgetr
 */
public class DefaultLogicalAreaTree extends BaseArtifact implements LogicalAreaTree
{
    private LogicalArea root;
    
    public DefaultLogicalAreaTree(IRI parentIRI)
    {
        super(parentIRI);
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
