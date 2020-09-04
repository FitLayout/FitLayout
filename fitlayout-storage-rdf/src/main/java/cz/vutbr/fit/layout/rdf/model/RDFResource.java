/**
 * RDFResource.java
 *
 * Created on 14. 1. 2016, 10:34:45 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import org.eclipse.rdf4j.model.IRI;

/**
 * A RDF resource with an URI.
 * @author burgetr
 */
public interface RDFResource
{
    
    /**
     * Obtains the URI of the resource in the RDF storage.
     * @return the resource URI
     */
    public IRI getIri();

}
