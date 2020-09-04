/**
 * RDFAreaTree.java
 *
 * Created on 16. 1. 2016, 20:47:28 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class RDFAreaTree extends DefaultAreaTree implements RDFResource
{
    protected IRI iri;
    protected Map<IRI, RDFArea> areaIris;
    protected Map<IRI, RDFLogicalArea> logicalAreaIris;


    public RDFAreaTree(Page page, IRI iri)
    {
        super(page);
        this.iri = iri;
    }
    
    public RDFAreaTree(AreaTree src, IRI iri)
    {
        super(src);
        this.iri = iri;
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

    public void setIri(IRI iri)
    {
        this.iri = iri;
    }

    public IRI getPageIri()
    {
        if (getPage() != null && getPage() instanceof RDFPage)
            return ((RDFPage) getPage()).getIri();
        else
            return null;
    }
    
    public Map<IRI, RDFArea> getAreaUris()
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
    
}
