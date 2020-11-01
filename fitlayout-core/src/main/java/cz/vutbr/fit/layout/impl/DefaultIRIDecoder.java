/**
 * DefaultIRIDecoder.java
 *
 * Created on 1. 11. 2020, 16:36:38 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;


/**
 * A default implementation of IRI decoder.
 * 
 * @author burgetr
 */
public class DefaultIRIDecoder implements IRIDecoder
{
    private Map<String, String> prefixUris; // prefix -> URI
    private Map<String, String> uriPrefixes; // URI -> prefix

    
    public DefaultIRIDecoder()
    {
        prefixUris = new HashMap<>();
        uriPrefixes = new HashMap<>();
        initPrefixes();
    }
    
    protected void initPrefixes()
    {
        addPrefix("rdf", RDF.NAMESPACE);
        addPrefix("rdfs", RDFS.NAMESPACE);
        addPrefix("box", BOX.NAMESPACE);
        addPrefix("segm", SEGM.NAMESPACE);
        addPrefix("fl", FL.NAMESPACE);
    }
    
    /**
     * Adds a new prefix to be used.
     * @param prefix the prefix string
     * @param uri the corresponding IRI prefix
     */
    public void addPrefix(String prefix, String uri)
    {
        prefixUris.put(prefix, uri);
        uriPrefixes.put(uri, prefix);
    }
    
    /**
     * Gets a map that assigns uris to prefix names.
     * @return the map
     */
    @Override
    public Map<String, String> getPrefixUris()
    {
        return prefixUris;
    }
    
    /**
     * Gets a map that assigns prefix names to uris.
     * @return the map
     */
    public Map<String, String> getUriPrefixes()
    {
        return uriPrefixes;
    }
    
    /**
     * Gets the prefix declaration string (e.g. for SPARQL) containing the currenly defined prefixes.
     * @return the prefix declaration string
     */
    @Override
    public String declarePrefixes()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : prefixUris.entrySet())
        {
            sb.append("PREFIX ")
                .append(entry.getKey()).append(": <")
                .append(entry.getValue()).append("> ");
        }
        return sb.toString();
    }
    
    /**
     * Converts an IRI to a prefixed string.
     * @param iri
     * @return
     */
    @Override
    public String encodeIri(IRI iri)
    {
        String ret = iri.toString();
        for (Map.Entry<String, String> entry : uriPrefixes.entrySet())
        {
            if (ret.startsWith(entry.getKey()))
            {
                ret = ret.replace(entry.getKey(), entry.getValue() + ":");
                break;
            }
        }
        return ret;
    }
    
    /**
     * Converts a prefixed string to an IRI
     * @param shortIri
     * @return
     */
    @Override
    public IRI decodeIri(String shortIri) throws IllegalArgumentException
    {
        String ret = shortIri;
        for (Map.Entry<String, String> entry : prefixUris.entrySet())
        {
            if (ret.startsWith(entry.getKey() + ":"))
            {
                ret = ret.replace(entry.getKey() + ":", entry.getValue());
                break;
            }
        }
        ValueFactory vf = SimpleValueFactory.getInstance();
        return vf.createIRI(ret);
    }

}
