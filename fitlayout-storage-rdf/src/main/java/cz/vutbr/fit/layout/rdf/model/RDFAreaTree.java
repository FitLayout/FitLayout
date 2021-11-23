/**
 * RDFAreaTree.java
 *
 * Created on 16. 1. 2016, 20:47:28 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;

/**
 * 
 * @author burgetr
 */
public class RDFAreaTree extends DefaultAreaTree implements RDFResource, RDFArtifact
{
    private Set<Statement> additionalStatements;
    protected Map<IRI, RDFArea> areaIris;
    protected Map<IRI, RDFLogicalArea> logicalAreaIris;


    public RDFAreaTree(IRI parentIri, IRI pageIri)
    {
        super(parentIri, pageIri);
    }
    
    public RDFAreaTree(AreaTree src, IRI targetIri)
    {
        super(src);
        setIri(targetIri);
    }

    public Map<IRI, RDFArea> getAreaIris()
    {
        return areaIris;
    }

    public void setAreaIris(Map<IRI, RDFArea> areaIris)
    {
        this.areaIris = areaIris;
    }
    
    public RDFArea findAreaByIri(IRI iri)
    {
        if (areaIris != null)
            return areaIris.get(iri);
        else
            return null;
    }

    public Map<IRI, RDFLogicalArea> getLogicalAreaIris()
    {
        return logicalAreaIris;
    }

    public void setLogicalAreaUris(Map<IRI, RDFLogicalArea> logicalAreaIris)
    {
        this.logicalAreaIris = logicalAreaIris;
    }
    
    public RDFLogicalArea findLogicalAreaByUri(IRI iri)
    {
        if (logicalAreaIris != null)
            return logicalAreaIris.get(iri);
        else
            return null;
    }

    public void setAdditionalStatements(Set<Statement> additionalStatements)
    {
        this.additionalStatements = additionalStatements;
    }

    @Override
    public Set<Statement> getAdditionalStatements()
    {
        return additionalStatements;
    }

    @Override
    public void recompute()
    {
        if (getRoot() != null)
            recursiveInvalidateStyle(getRoot());
    }
    
    private void recursiveInvalidateStyle(Area root)
    {
        root.childrenChanged();
        for (Area child : root.getChildren())
            recursiveInvalidateStyle(child);
    }
    
}
