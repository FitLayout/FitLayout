package cz.vutbr.fit.layout.rdf;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.impl.DefaultTreeContentRect;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.GenericTreeNode;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFOrderedResource;
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
    
    /**
     * Tags available in the repository. Initialized using loadTags().
     */
    private Map<IRI, Tag> repositoryTags;
    
    
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
    
    /**
     * Applies common ContentRect properties to a target rect.
     * 
     * @param con repository connection used to load additional triples
     * @param pred the property predicate
     * @param value the property value
     * @param rect the target rect
     * @return {@code true} when the property was applied, {@code false} when it was ignored
     */
    protected boolean processContentRectProperty(RepositoryConnection con, IRI pred, Value value, 
            DefaultTreeContentRect<?> rect)
    {
        boolean ret = true;
        if (BOX.backgroundColor.equals(pred)) 
        {
            String bgColor = value.stringValue();
            //bgColor = bgColor.substring(1,bgColor.length());
            rect.setBackgroundColor( Serialization.decodeHexColor( bgColor ) );
        }
        else if (BOX.backgroundSeparated.equals(pred))
        {
            if (value instanceof Literal)
                rect.setBackgroundSeparated(((Literal) value).booleanValue());
        }
        else if (BOX.hasBottomBorder.equals(pred)) 
        {
            if (value instanceof IRI)
            {
                Border border = createBorder(con, (IRI) value);
                rect.setBorderStyle(Side.BOTTOM, border);
            }
        }
        else if (BOX.hasLeftBorder.equals(pred)) 
        {
            if (value instanceof IRI)
            {
                Border border = createBorder(con, (IRI) value);
                rect.setBorderStyle(Side.LEFT, border);
            }
        }
        else if (BOX.hasRightBorder.equals(pred)) 
        {
            if (value instanceof IRI)
            {
                Border border = createBorder(con, (IRI) value);
                rect.setBorderStyle(Side.RIGHT, border);
            }
        }
        else if (BOX.hasTopBorder.equals(pred)) 
        {
            if (value instanceof IRI)
            {
                Border border = createBorder(con, (IRI) value);
                rect.setBorderStyle(Side.TOP, border);
            }
        }
        else
        {
            ret = false;
        }
        return ret;
    }
    
    /**
     * Applies common text style properties to a target style.
     * 
     * @param pred the property predicate
     * @param value the property value
     * @param style the target rect style
     * @return {@code true} when the property was applied, {@code false} when it was ignored
     */
    protected boolean processStyleProperty(IRI pred, Value value, RDFTextStyle style)
    {
        boolean ret = true;
        if (BOX.underline.equals(pred)) 
        {
            if (value instanceof Literal)
                style.underline = ((Literal) value).floatValue();
        }
        else if (BOX.lineThrough.equals(pred)) 
        {
            if (value instanceof Literal)
                style.lineThrough = ((Literal) value).floatValue();
        }
        else if (BOX.fontSize.equals(pred)) 
        {
            if (value instanceof Literal)
                style.fontSize = ((Literal) value).floatValue();
        }
        else if (BOX.fontStyle.equals(pred)) 
        {
            if (value instanceof Literal)
                style.fontStyle = ((Literal) value).floatValue();
        }
        else if (BOX.fontWeight.equals(pred)) 
        {
            if (value instanceof Literal)
                style.fontWeight = ((Literal) value).floatValue();
        }
        else if (BOX.contentLength.equals(pred)) 
        {
            if (value instanceof Literal)
                style.contentLength = ((Literal) value).intValue();
        }
        else
        {
            ret = false;
        }
        return ret;
    }
    
    protected Border createBorder(RepositoryConnection con, IRI borderIri)
    {
        Border ret = new Border();
        
        try (RepositoryResult<Statement> result = con.getStatements(borderIri, null, null))
        {
            for (Statement st : result)
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
        }
        
        return ret;
    }
    
    protected Map.Entry<String, String> createAttribute(RepositoryConnection con, IRI attrIri)
    {
        String name = null;
        String avalue = null;
        
        try (RepositoryResult<Statement> result = con.getStatements(attrIri, null, null))
        {
            for (Statement st : result)
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
        }
        if (name != null && avalue != null)
            return new AbstractMap.SimpleEntry<String, String>(name, avalue);
        else
            return null;
    }
    
    protected Rectangular createBounds(RepositoryConnection con, IRI boundsIri)
    {
        Integer x = null;
        Integer y = null;
        Integer width = null;
        Integer height = null;
        
        try (RepositoryResult<Statement> result = con.getStatements(boundsIri, null, null))
        {
            for (Statement st : result)
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
     * Loads the source area tree artifact of this area tree.
     * @param areaTreeIri the source page IRI
     * @param repo the repository used for loading the page artifact.
     * @return the page artifact or {@code null} when not specified or not found
     */
    protected RDFAreaTree getSourceAreaTree(IRI areaTreeIri, ArtifactRepository repo)
    {
        RDFAreaTree atree = (RDFAreaTree) repo.getArtifact(areaTreeIri);
        return atree;
    }
    
    /**
     * Loads available tags from a RDF repository.
     * 
     * @param repo the repository to use as the tag source
     */
    protected void loadTags(RDFArtifactRepository repo)
    {
        repositoryTags = new HashMap<>();
        for (Tag tag : repo.getTags())
            repositoryTags.put(tag.getIri(), tag);
    }
    
    /**
     * Gets an instance of a tag defined in the repository.
     * 
     * @param tagIri the IRI of the tag
     * @return a tag defined for the IRI or a "x" type tag the tag is not defined
     */
    protected Tag getTag(IRI tagIri)
    {
        if (repositoryTags == null)
            repositoryTags = new HashMap<>();
        Tag tag = repositoryTags.get(tagIri);
        if (tag == null)
        {
            tag = new DefaultTag(tagIri, "x", tagIri.getLocalName());
            repositoryTags.put(tagIri, tag);
        }
        return tag;
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
    
    //=================================================================================
    
    protected Model execArtifactReadQuery(RDFArtifactRepository artifactRepo, String query)
    {
        return artifactRepo.getStorage().executeSafeQuery(query, IsolationLevels.SNAPSHOT_READ);
    }
    
    public Value getPropertyValue(RepositoryConnection con, Resource subject, IRI predicate) throws StorageException
    {
        Value ret = null;
        try (RepositoryResult<Statement> result = con.getStatements(subject, predicate, null, true)) {
            if (result.hasNext())
                ret = result.next().getObject();
        }
        return ret;
    }
    //=================================================================================
    
    /**
     * Checks that each child area has a document order assigned and that the children
     * are ordered by the assigned order.
     * @param <T>
     * @param root
     */
    protected <T extends GenericTreeNode<T>> void checkChildOrderValues(GenericTreeNode<T> root)
    {
        int offset = 0;
        // ensure that each child has an assigned order
        for (T childArea : root.getChildren())
        {
            final RDFOrderedResource child = (RDFOrderedResource) childArea;
            if (child.getDocumentOrder() == -1) // order not present
            {
                int newOrder = -1;
                // first, try to use the order of the first child (if any)
                if (childArea.getChildCount() != 0)
                    newOrder = ((RDFOrderedResource) childArea.getChildAt(0)).getDocumentOrder();
                // if failed, use the parent order + an offset
                if (newOrder == -1)
                {
                    newOrder = ((RDFOrderedResource) root).getDocumentOrder() + offset;
                    offset++;
                }
                child.setDocumentOrder(newOrder);
            }
            // recursively apply to child nodes
            checkChildOrderValues(childArea);
        }
        // sort children by order
        root.getChildren().sort(new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return ((RDFOrderedResource) o1).getDocumentOrder()
                        - ((RDFOrderedResource) o2).getDocumentOrder();
            }
        });
    }
    
    //=================================================================================
    
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
