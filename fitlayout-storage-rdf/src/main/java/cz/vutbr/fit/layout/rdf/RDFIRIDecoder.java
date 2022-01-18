/**
 * RDFIRIDecoder.java
 *
 * Created on 1. 11. 2020, 16:43:39 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import cz.vutbr.fit.layout.impl.DefaultIRIDecoder;
import cz.vutbr.fit.layout.ontology.RESOURCE;

/**
 * An IRI decoder that adds the 'r' prefix for RDF resources.
 * 
 * @author burgetr
 */
public class RDFIRIDecoder extends DefaultIRIDecoder
{

    public RDFIRIDecoder()
    {
        super();
    }

    @Override
    protected void initPrefixes()
    {
        super.initPrefixes();
        addPrefix("r", RESOURCE.NAMESPACE);
    }

}
