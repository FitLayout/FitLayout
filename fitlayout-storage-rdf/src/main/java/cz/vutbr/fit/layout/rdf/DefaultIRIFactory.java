/**
 * DefaultIRIFactory.java
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
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * A default FitLayout IRI factory.
 * 
 * @author burgetr
 */
public class DefaultIRIFactory implements IRIFactory
{
    public static final String NAMESPACE = "http://fitlayout.github.io/resource/";
    public static final String PREFIX = "r";

    private final ValueFactory factory;
    
    public DefaultIRIFactory()
    {
        factory = SimpleValueFactory.getInstance();
    }
    
    /**
     * Creates a page set IRI from its name.
     * @param name the name of the page set
     * @return the created IRI
     */
    @Override
    public IRI createPageSetURI(String name)
    {
        String res = name.replace(' ', '_');
        res = UriEscape.escapeUriPathSegment(res);
        return factory.createIRI(NAMESPACE, "pset-" + res);
    }
    
    @Override
    public IRI createArtifactIri(long seq)
    {
        return factory.createIRI(NAMESPACE, "art" + seq);
    }
    
    @Override
    public IRI createArtifactIri(String id)
    {
        return factory.createIRI(NAMESPACE, "art-" + id);
    }
    
    @Override
    public IRI createBoxURI(IRI pageUri, Box box)
    {
        return factory.createIRI(String.valueOf(pageUri) + "#b" + box.getId());
    }
    
    @Override
    public IRI createBoundsURI(IRI boxUri, String type)
    {
        final String localName = boxUri.getLocalName() + "-rect-" + type;
        return factory.createIRI(boxUri.getNamespace(), localName);
    }
    
    @Override
    public IRI createBorderURI(IRI boxUri, String side)
    {
        final String localName = boxUri.getLocalName() + "B" + side;
        return factory.createIRI(boxUri.getNamespace(), localName);
    }
    
    @Override
    public IRI createAttributeURI(IRI boxUri, String name)
    {
        final String localName = boxUri.getLocalName() + "-attr-" + name;
        return factory.createIRI(boxUri.getNamespace(), localName);
    }
    
    @Override
    public IRI createContentObjectURI(IRI pageUri, int seq)
    {
        return factory.createIRI(String.valueOf(pageUri) + "#o" + seq);
    }

    @Override
    public IRI createAreaURI(IRI areaTreeNode, Area area) 
    {
        return factory.createIRI(String.valueOf(areaTreeNode) + "#a" + area.getId());
    }
    
    @Override
    public IRI createLogicalAreaURI(IRI areaTreeNode, int cnt) 
    {
        return factory.createIRI(String.valueOf(areaTreeNode) + "#l" + cnt);
    }
    
    @Override
    public IRI createTagSupportURI(IRI areaUri, Tag tag) 
    {
        return factory.createIRI(String.valueOf(areaUri) + "-" + getTagDesc(tag));
    }
    
    @Override
    public IRI createTagURI(Tag tag) 
    {
        return factory.createIRI(NAMESPACE, "tag-" + getTagDesc(tag));
    }
    
    private String getTagDesc(Tag tag) 
    {
        return tag.getType().replaceAll("\\.", "-") + "--" + tag.getValue();
    }
    
    @Override
    public IRI createTextChunkURI(IRI chunkSetUri, TextChunk chunk)
    {
        return factory.createIRI(String.valueOf(chunkSetUri) + "#c" + chunk.getId());
    }
    
    /**
     * Creates a sequence IRI from its name.
     * @param name the name of the sequence (alphabetical characters only)
     * @return the created IRI
     */
    @Override
    public IRI createSequenceURI(String name)
    {
        return factory.createIRI(NAMESPACE, "seq-" + name);
    }
    
}
