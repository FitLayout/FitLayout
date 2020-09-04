/**
 * RDFContentObject.java
 *
 * Created on 15. 11. 2016, 13:49:22 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.ContentObject;

/**
 * Generic ContentObject implementation.
 *  
 * @author burgetr
 */
public class RDFContentObject implements RDFResource, ContentObject
{
    private IRI iri;
    
    public RDFContentObject(IRI iri)
    {
        this.iri = iri;
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

    @Override
    public String toString()
    {
        return "(unknown object)";
    }
    
}
