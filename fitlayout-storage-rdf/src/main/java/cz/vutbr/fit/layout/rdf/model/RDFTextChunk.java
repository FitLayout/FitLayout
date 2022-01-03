/**
 * RDFTextChunk.java
 *
 * Created on 10. 4. 2021, 19:12:46 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class RDFTextChunk extends DefaultTextChunk implements RDFResource
{
    protected IRI iri;
    protected int documentOrder;

    public RDFTextChunk(IRI uri)
    {
        super();
        setIri(uri);
    }
    
    public RDFTextChunk(Rectangular r, Area sourceArea, Box sourceBox, IRI uri)
    {
        super(r, sourceArea, sourceBox);
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

    public int getDocumentOrder()
    {
        return documentOrder;
    }

    public void setDocumentOrder(int documentOrder)
    {
        this.documentOrder = documentOrder;
    }

    @Override
    public String toString()
    {
        return "<" + getIri() + "> '" + getText() + "'";
    }
    
}
