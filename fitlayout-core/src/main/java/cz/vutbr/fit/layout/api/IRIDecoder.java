/**
 * IRIDecoder.java
 *
 * Created on 1. 11. 2020, 16:33:12 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

/**
 * An IRI encoder / decoder. It maintains a set of prexifes and allows converting an IRI
 * to its prefixed form and back to full form.
 *  
 * @author burgetr
 */
public interface IRIDecoder
{

    /**
     * Converts an IRI to its prefixed string.
     * @param iri the IRI to encode
     * @return the prefixed IRI string
     */
    public String encodeIri(IRI iri);

    /**
     * Converts a prefixed string to an IRI.
     * @param shortIri the prefixed IRI string.
     * @return the decoded IRI
     * @throws IllegalArgumentException when the given IRI could not be decoded (has an invalid format)
     */
    public IRI decodeIri(String shortIri) throws IllegalArgumentException;
    
    /**
     * Gets a map that assigns uris to known prefix names.
     * @return the map prefix name to URI prefix
     */
    public Map<String, String> getPrefixUris();

    /**
     * Generates a prefix declaration string (e.g. for SPARQL) containing the currenly known prefixes.
     * @return the prefix declaration string
     */
    public String declarePrefixes();

}
