package cz.vutbr.fit.layout.rdf;
import java.util.AbstractMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFPage;

/**
 * ModelLoader.java
 *
 * Created on 17. 1. 2016, 17:19:32 by burgetr
 */

/**
 * Model loader base.
 * @author burgetr
 */
public abstract class ModelLoaderBase extends ModelTransformer
{
    private static Logger log = LoggerFactory.getLogger(ModelLoaderBase.class);
    
    public ModelLoaderBase(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    protected IRI getPredicateIriValue(Model model, IRI subject, IRI predicate)
    {
        Iterable<Statement> typeStatements = model.getStatements(subject, predicate, null);
        for (Statement st : typeStatements)
        {
            if (st.getObject() instanceof IRI)
                return (IRI) st.getObject();
        }
        return null;
    }
    
    protected Border createBorder(Model model, IRI uri)
    {
        Border ret = new Border();
        
        for (Statement st : model.filter(uri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (BOX.borderColor.equals(pred)) 
            {
                ret.setColor(Serialization.decodeHexColor(value.stringValue()));
            }
            else if (BOX.borderWidth.equals(pred))
            {
                if (value instanceof Literal)
                    ret.setWidth(((Literal) value).intValue());
            }
            else if (BOX.borderStyle.equals(pred))
            {
                String style = value.stringValue();
                try {
                    ret.setStyle(Border.Style.valueOf(style));
                } catch (IllegalArgumentException r) {
                    log.error("Invalid style value: {}", style);
                }
            }
        }
        
        return ret;
    }
    
    protected Map.Entry<String, String> createAttribute(Model model, IRI uri)
    {
        String name = null;
        String avalue = null;
        for (Statement st : model.filter(uri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            if (RDFS.LABEL.equals(pred))
            {
                if (value instanceof Literal)
                    name = ((Literal) value).stringValue();
            }
            else if (RDF.VALUE.equals(pred))
            {
                if (value instanceof Literal)
                    avalue = ((Literal) value).stringValue();
            }
        }
        if (name != null && avalue != null)
            return new AbstractMap.SimpleEntry<String, String>(name, avalue);
        else
            return null;
    }
    
    protected Rectangular createBounds(Model model, IRI iri)
    {
        Integer x = null;
        Integer y = null;
        Integer width = null;
        Integer height = null;
        for (Statement st : model.filter(iri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            if (BOX.positionX.equals(pred))
            {
                if (value instanceof Literal)
                    x = ((Literal) value).intValue();
            }
            else if (BOX.positionY.equals(pred))
            {
                if (value instanceof Literal)
                    y = ((Literal) value).intValue();
            }
            else if (BOX.width.equals(pred))
            {
                if (value instanceof Literal)
                    width = ((Literal) value).intValue();
            }
            else if (BOX.height.equals(pred))
            {
                if (value instanceof Literal)
                    height = ((Literal) value).intValue();
            }
        }        
        if (x != null && y != null && width != null && height != null)
            return new Rectangular(x, y, x + width - 1, y + height - 1);
        else
            return null;
    }
    
    /**
     * Finds the source page IRI in the page model
     * @param model The page model
     * @param areaTreeIri area tree IRI
     * @return the source page IRI or {@code null} when not defined
     */
    protected IRI getSourcePageIri(Model model, IRI areaTreeIri)
    {
        return getPredicateIriValue(model, areaTreeIri, SEGM.hasSourcePage);
    }
    
    /**
     * Loads the source page artifact of the area tree.
     * @param pageIri the source page IRI
     * @param repo the repository used for loading the page artifact.
     * @return the page artifact or {@code null} when not specified or not found
     */
    protected RDFPage getSourcePage(IRI pageIri, ArtifactRepository repo)
    {
        RDFPage page = (RDFPage) repo.getArtifact(pageIri);
        return page;
    }
    
    /**
     * Finds the source page IRI in the page model
     * @param model The page model
     * @param logicalTreeIri logical area tree IRI
     * @return the source page IRI or {@code null} when not defined
     */
    protected IRI getSourceAreaTreeIri(Model model, IRI logicalTreeIri)
    {
        return getPredicateIriValue(model, logicalTreeIri, SEGM.hasAreaTree);
    }
    
    /**
     * Loads the source page artifact of the area tree.
     * @param pageIri the source page IRI
     * @param repo the repository used for loading the page artifact.
     * @return the page artifact or {@code null} when not specified or not found
     */
    protected RDFAreaTree getSourceAreaTree(IRI areaTreeIri, ArtifactRepository repo)
    {
        RDFAreaTree atree = (RDFAreaTree) repo.getArtifact(areaTreeIri);
        return atree;
    }
    
    /**
     * Creates a tag from the given tag IRI and the tag data model.
     * @param tagModel
     * @param tagIri
     * @return the created tag or {@code null} when no tag info is found
     * @throws RepositoryException
     */
    protected Tag createTag(Model tagModel, IRI tagIri) throws RepositoryException
    {
        String name = null;
        String type = null;
        for (Statement st : tagModel.filter(tagIri, null, null))
        {
            IRI pred = st.getPredicate();
            if (SEGM.hasName.equals(pred))
                name = st.getObject().stringValue();
            else if (SEGM.hasType.equals(pred))
                type = st.getObject().stringValue();
        }
        if (name != null && type != null)
            return new DefaultTag(type, name);
        else
            return null;
    }
    
    /**
     * Creates SPAQRL union for the given data properties.
     * @param dataObjectProperties
     * @return
     */
    protected String getDataPropertyUnion(String[] dataObjectProperties)
    {
        StringBuilder ret = new StringBuilder();
        for (String p : dataObjectProperties)
        {
            if (ret.length() > 0)
                ret.append(" UNION ");
            ret.append("{?a ").append(p).append(" ?s}");
        }
        return ret.toString();
    }
    
    protected static class RDFTextStyle
    {
        // average values loaded from RDF
        public float fontSize = 0;
        public float fontWeight = 0;
        public float fontStyle = 0;
        public float underline = 0;
        public float lineThrough = 0;
        // content length loaded from RDF
        public int contentLength = 1; // assume 1 element when nothing else is specified
        
        public TextStyle toTextStyle()
        {
            TextStyle ret = new TextStyle();
            ret.setFontSizeSum(fontSize * contentLength);
            ret.setFontWeightSum(fontWeight * contentLength);
            ret.setFontStyleSum(fontStyle * contentLength);
            ret.setUnderlineSum(underline * contentLength);
            ret.setLineThroughSum(lineThrough * contentLength);
            ret.setContentLength(contentLength);
            return ret;
        }
        
    }
    
}
