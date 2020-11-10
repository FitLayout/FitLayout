/**
 * RDFBox.java
 *
 * Created on 14. 1. 2016, 10:31:25 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Box;

/**
 * 
 * @author burgetr
 */
public class RDFBox extends DefaultBox implements RDFResource
{
    protected IRI iri;
    protected int documentOrder;

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
        setId(Integer.parseInt(uri.getLocalName()));
    }

    @Override
    public String toString()
    {
        String ret = getId() + " ";
        if (getType() == Box.Type.TEXT_CONTENT)
        {
            ret += getText();
        }
        else if (getType() == Box.Type.REPLACED_CONTENT)
        {
            if (getContentObject() != null)
                ret += getContentObject().toString();
            else
                ret += "(null object)";
        }
        else
        {
            ret += "<" + getTagName();
            if (getAttribute("id") != null)
                ret += " id=" + getAttribute("id");
            if (getAttribute("class") != null)
                ret += " class=" + getAttribute("class");
            ret += ">";
        }
        return ret;
    }
    
}
