/**
 * RDFBox.java
 *
 * Created on 14. 1. 2016, 10:31:25 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultBox;

/**
 * 
 * @author burgetr
 */
public class RDFBox extends DefaultBox implements RDFOrderedResource
{
    protected IRI iri;

    public RDFBox(IRI uri)
    {
        super();
        setIri(uri);
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

    public void setIri(IRI uri)
    {
        this.iri = uri;
    }

    @Override
    public int getDocumentOrder()
    {
        return getOrder();
    }

    @Override
    public void setDocumentOrder(int order)
    {
        setOrder(order);
    }

}
