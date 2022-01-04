/**
 * RDFTag.java
 *
 * Created on 4. 1. 2022, 17:43:16 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultTag;

/**
 * 
 * @author burgetr
 */
public class RDFTag extends DefaultTag implements RDFResource
{
    private IRI iri;
    
    public RDFTag(IRI iri, String type, String value)
    {
        super(type, value);
        this.iri = iri;
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

}
