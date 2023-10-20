/**
 * RDFTag.java
 *
 * Created on 20. 10. 2023, 11:29:06 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultTag;

/**
 * A tag defined using the RDF repository configuration with an optional context where it was defined.
 * 
 * @author burgetr
 */
public class RDFTag extends DefaultTag
{
    private IRI context;

    public RDFTag(IRI iri, String type, String name)
    {
        super(iri, type, name);
    }

    public RDFTag(IRI iri, String type, String name, IRI context)
    {
        super(iri, type, name);
        this.context = context;
    }

    public IRI getContext()
    {
        return context;
    }

    public void setContext(IRI context)
    {
        this.context = context;
    }

}
