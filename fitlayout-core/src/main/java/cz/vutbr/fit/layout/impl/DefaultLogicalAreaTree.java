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
    private IRI areaTreeIri;
    private LogicalArea root;
    
    public DefaultLogicalAreaTree(IRI parentIRI)
    {
        super(parentIRI);
        setAreaTreeIri(parentIRI);
    }
    
    public DefaultLogicalAreaTree(IRI parentIRI, IRI areaTreeIri)
    {
        super(parentIRI);
        setAreaTreeIri(areaTreeIri);
    }
    
    @Override
    public IRI getArtifactType()
    {
        return SEGM.LogicalAreaTree;
    }

    @Override
    public IRI getAreaTreeIri()
    {
        return areaTreeIri;
    }

    public void setAreaTreeIri(IRI areaTreeIri)
    {
        this.areaTreeIri = areaTreeIri;
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
