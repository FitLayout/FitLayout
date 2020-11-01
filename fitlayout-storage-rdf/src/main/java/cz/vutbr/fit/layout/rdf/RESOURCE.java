/**
 * RESOURCE.java
 *
 * Created on 9. 1. 2016, 10:33:35 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.unbescape.uri.UriEscape;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Tag;

/**
 * FitLayout resource IRI generation.
 * 
 * @author burgetr
 */
public class RESOURCE
{
    public static final String NAMESPACE = "http://fitlayout.github.io/resource/";
    public static final String PREFIX = "r";

    private static final ValueFactory factory = SimpleValueFactory.getInstance();
    
    /**
     * Creates a page set IRI from its name.
     * @param name the name of the page set
     * @return the created IRI
     */
    public static IRI createPageSetURI(String name)
    {
        String res = name.replace(' ', '_');
        res = UriEscape.escapeUriPathSegment(res);
        return factory.createIRI(NAMESPACE, "pset-" + res);
    }
    
    public static IRI createArtifactIri(long seq)
    {
        return factory.createIRI(NAMESPACE, "art" + seq);
    }
    
    public static IRI createArtifactIri(String id)
    {
        return factory.createIRI(NAMESPACE, "art-" + id);
    }
    
    public static IRI createBoxURI(IRI pageUri, Box box)
    {
        return factory.createIRI(pageUri.toString() + '#' + box.getId());
    }
    
    public static IRI createBorderURI(IRI boxUri, String side)
    {
        String localName = boxUri.getLocalName() + "B" + side;
        return factory.createIRI(boxUri.getNamespace(), localName);
    }
    
    public static IRI createAttributeURI(IRI boxUri, String name)
    {
        String localName = boxUri.getLocalName() + "-attr-" + name;
        return factory.createIRI(boxUri.getNamespace(), localName);
    }
    
    public static IRI createAreaURI(IRI areaTreeNode, Area area) 
    {
        return factory.createIRI(areaTreeNode.toString() + "#a" + area.getId());
    }
    
    public static IRI createLogicalAreaURI(IRI areaTreeNode, int cnt) 
    {
        return factory.createIRI(areaTreeNode.toString() + "#l" + cnt);
    }
    
    public static IRI createTagSupportURI(IRI areaUri, Tag tag) 
    {
        return factory.createIRI(areaUri.toString() + "-" + getTagDesc(tag));
    }
    
    public static IRI createTagURI(Tag tag) 
    {
        return factory.createIRI(RESOURCE.NAMESPACE, "tag-" + getTagDesc(tag));
    }
    
    private static String getTagDesc(Tag tag) 
    {
        return tag.getType().replaceAll("\\.", "-") + "--" + tag.getValue();
    }
    
    /**
     * Creates a sequence IRI from its name.
     * @param name the name of the sequence (alphabetical characters only)
     * @return the created IRI
     */
    public static IRI createSequenceURI(String name)
    {
        return factory.createIRI(NAMESPACE, "seq-" + name);
    }
    
    
    private RESOURCE()
    {
        //static access only
    }

    
}
