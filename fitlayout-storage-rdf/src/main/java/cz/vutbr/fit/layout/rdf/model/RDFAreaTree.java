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

/**
 * 
 * @author burgetr
 */
public class RDFAreaTree extends DefaultAreaTree implements RDFResource
{
    protected Map<IRI, RDFArea> areaIris;
    protected Map<IRI, RDFLogicalArea> logicalAreaIris;


    public RDFAreaTree(IRI pageIri)
    {
        super(pageIri);
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
    
}
