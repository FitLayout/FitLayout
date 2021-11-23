/**
 * BoxModelLoader.java
 *
 * Created on 13. 1. 2016, 23:49:14 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.model.RDFBox;
import cz.vutbr.fit.layout.rdf.model.RDFContentImage;
import cz.vutbr.fit.layout.rdf.model.RDFContentObject;
import cz.vutbr.fit.layout.rdf.model.RDFPage;

/**
 * This class implements creating a RDFPage from the RDF models.
 * @author burgetr
 */
public class BoxModelLoader extends ModelLoaderBase implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(BoxModelLoader.class);
    
    private static final String[] dataObjectProperties = new String[] { 
            "box:hasTopBorder",
            "box:hasBottomBorder",
            "box:hasLeftBorder",
            "box:hasRightBorder",
            "box:hasAttribute",
            "box:bounds",
            "box:visualBounds",
            "box:contentBounds"
    };

    public BoxModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo)
            throws RepositoryException
    {
        return constructPage(artifactRepo, artifactIri);
    }

    private RDFPage constructPage(RDFArtifactRepository artifactRepo, IRI pageIri) throws RepositoryException
    {
        Model pageModel = artifactRepo.getStorage().getSubjectModel(pageIri);
        if (pageModel.size() > 0)
        {
            //create the page
            PageInfo info = new PageInfo(pageModel, pageIri);
            URL srcURL;
            try {
                srcURL = new URL(info.getUrl());
            } catch (MalformedURLException e) {
                try {
                    srcURL = new URL("http://no/url");
                } catch (MalformedURLException e1) {
                    srcURL = null;
                }
            }
            RDFPage page = new RDFPage(srcURL);
            info.applyToPage(page);
            
            //load the models
            Model boxTreeModel = getBoxModelForPage(artifactRepo, pageIri);
            Model dataModel = getBoxDataModelForPage(artifactRepo, pageIri);
            //create the box tree
            Map<IRI, RDFBox> boxes = new LinkedHashMap<IRI, RDFBox>();
            RDFBox root = constructBoxTree(artifactRepo.getStorage(), boxTreeModel, dataModel, pageIri,
                    boxes, page.getAdditionalStatements()); 
            page.setRoot(root);
            page.setBoxIris(boxes);
            if (page.getWidth() == -1 && page.getHeight() == -1) //when the page width and height was not set
            {
                page.setWidth(root.getWidth());
                page.setHeight(root.getHeight());
            }
            
            return page;
        }
        else
            return null;
    }
    
    /**
     * Constructs a tree of boxes based on the given model. The URIs of the created boxes are put to
     * a map that allows to later obtain a box by its IRI.
     * @param boxTreeModel the source model
     * @param boxes the destination map of URIs
     * @return the root box or {@code null} if the provided model does not have a tree structure
     * @throws RepositoryException
     */
    private RDFBox constructBoxTree(RDFStorage storage, Model boxTreeModel, Model dataModel,
            IRI pageIri, Map<IRI, RDFBox> boxes, Collection<Statement> additionalStatements) throws RepositoryException
    {
        //find all boxes
        for (Resource res : boxTreeModel.subjects())
        {
            if (res instanceof IRI)
            {
                RDFBox box = createBoxFromModel(storage, boxTreeModel, dataModel, pageIri,
                        (IRI) res, additionalStatements);
                boxes.put((IRI) res, box);
            }
        }
        List<RDFBox> rootBoxes = new ArrayList<RDFBox>(boxes.values());
        //construct the tree
        for (Statement st : boxTreeModel.filter(null, BOX.isChildOf, null))
        {
            if (st.getSubject() instanceof IRI && st.getObject() instanceof IRI)
            {
                RDFBox parent = boxes.get(st.getObject());
                RDFBox child = boxes.get(st.getSubject());
                if (parent != null && child != null)
                {
                    parent.appendChild(child);
                    rootBoxes.remove(child);
                }
            }
        }
        if (rootBoxes.size() == 1)
            return rootBoxes.get(0);
        else
        {
            log.error("Strange number of root boxes: {}", rootBoxes.toString());
            return null; //strange number of root nodes
        }
    }
    
    private RDFBox createBoxFromModel(RDFStorage storage, Model boxTreeModel, Model dataModel, 
            IRI pageIri, IRI boxIri, Collection<Statement> additionalStatements) throws RepositoryException
    {
        RDFBox box = new RDFBox(boxIri);
        box.setId(getIriFactory().decodeBoxId(boxIri));
        box.setTagName("");
        box.setType(Box.Type.ELEMENT);
        box.setDisplayType(Box.DisplayType.BLOCK);
        
        RDFTextStyle style = new RDFTextStyle();
        
        for (Statement st : boxTreeModel.filter(boxIri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (BOX.documentOrder.equals(pred))
            {
                if (value instanceof Literal)
                    box.setOrder(((Literal) value).intValue());
            }
            else if (BOX.visible.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setVisible(((Literal) value).booleanValue());
            }
            else if (BOX.backgroundColor.equals(pred)) 
            {
                String bgColor = value.stringValue();
                //bgColor = bgColor.substring(1,bgColor.length());
                box.setBackgroundColor( Serialization.decodeHexColor( bgColor ) );
            }
            else if (BOX.hasBackgroundImage.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    final RDFContentImage image = loadImage(storage, (IRI) value);
                    box.setBackgroundImagePng(image.getPngData());
                }
            }
            else if (BOX.backgroundSeparated.equals(pred))
            {
                if (value instanceof Literal)
                    box.setBackgroundSeparated(((Literal) value).booleanValue());
            }
            else if (BOX.color.equals(pred)) 
            {
                box.setColor(Serialization.decodeHexColor(value.stringValue()));
            }
            else if (BOX.underline.equals(pred)) 
            {
                if (value instanceof Literal)
                    style.underline = ((Literal) value).floatValue();
            }
            else if (BOX.lineThrough.equals(pred)) 
            {
                if (value instanceof Literal)
                    style.lineThrough = ((Literal) value).floatValue();
            }
            else if (BOX.fontFamily.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setFontFamily(value.stringValue());
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
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    box.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    box.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    box.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    box.setBorderStyle(Side.TOP, border);
                }
            }
            else if (BOX.text.equals(pred)) 
            {
                if (box.getType() != Type.REPLACED_CONTENT) //once it is a replaced box, do not change it back to text box
                    box.setType(Type.TEXT_CONTENT);
                box.setDisplayType(null); //text boxes have no display type
                box.setOwnText(value.stringValue());
            }
            else if (BOX.containsObject.equals(pred))
            {
                box.setType(Type.REPLACED_CONTENT);
                if (value instanceof IRI)
                {
                    final Value valueType = storage.getPropertyValue((IRI) value, RDF.TYPE);
                    if (BOX.Image.equals(valueType)) // treat images in a special way
                    {
                        final RDFContentImage obj = loadImage(storage, (IRI) value);
                        box.setContentObject(obj);
                    }
                    else // other objects than images
                    {
                        final RDFContentObject obj = new RDFContentObject((IRI) value);
                        box.setContentObject(obj);
                    }
                }
            }
            else if (BOX.bounds.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Rectangular rect = createBounds(dataModel, (IRI) value);
                    if (rect != null)
                        box.setBounds(rect);
                }
            }
            else if (BOX.visualBounds.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Rectangular rect = createBounds(dataModel, (IRI) value);
                    if (rect != null)
                        box.setVisualBounds(rect);
                }
            }
            else if (BOX.contentBounds.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Rectangular rect = createBounds(dataModel, (IRI) value);
                    if (rect != null)
                        box.setContentBounds(rect);
                }
            }
            else if (BOX.htmlTagName.equals(pred)) 
            {
                box.setTagName(value.stringValue());
            }
            else if (BOX.displayType.equals(pred))
            {
                final Box.DisplayType type = Serialization.decodeDisplayType(value.stringValue());
                if (type != null)
                    box.setDisplayType(type);
            }
            else if (BOX.hasAttribute.equals(pred))
            {
                if (value instanceof IRI)
                {
                    Map.Entry<String, String> attr = createAttribute(dataModel, (IRI) value);
                    if (attr != null)
                        box.setAttribute(attr.getKey(), attr.getValue());
                }
            }
            else
            {
                // the statement was not used, keep it in additional statements
                additionalStatements.add(st);
            }
        }
        box.setTextStyle(style.toTextStyle());
        
        return box;
    }

    /**
     * Loads an Image object from the storage.
     * @param storage
     * @param imageIri
     * @return
     */
    private RDFContentImage loadImage(RDFStorage storage, final IRI imageIri)
    {
        RDFContentImage obj = new RDFContentImage(imageIri);
        Value urlVal = storage.getPropertyValue(imageIri, BOX.imageUrl);
        if (urlVal != null && urlVal instanceof Literal)
        {
            try {
                obj.setUrl(((Literal) urlVal).stringValue());
            } catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
        Value dataVal = storage.getPropertyValue(imageIri, BOX.imageData);
        if (dataVal != null && dataVal instanceof Literal)
        {
            final String dataStr = dataVal.stringValue();
            try {
                obj.setPngData(Base64.getDecoder().decode(dataStr));
            } catch (IllegalArgumentException e) {
                obj.setPngData(null);
            }
        }
        return obj;
    }

    /**
     * Gets page box model from the unique page ID.
     * @param artifactRepo the repository to query 
     * @param pageIri the page IRI
     * @return The creayed model
     * @throws RepositoryException 
     */
    private Model getBoxModelForPage(RDFArtifactRepository artifactRepo, IRI pageIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type box:Box . "
                + "?s box:documentOrder ?ord . "
                + "?s box:belongsTo <" + String.valueOf(pageIri) + ">}"
                + " ORDER BY ?ord";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }

    /**
     * Gets the model of additional object properties of the boxes. It contains the data about the
     * bounds, borders, attributes and other object properties.
     * @param artifactRepo the repository to query 
     * @param pageIri the page IRI
     * @return The created model
     * @throws RepositoryException 
     */
    private Model getBoxDataModelForPage(RDFArtifactRepository artifactRepo, IRI pageIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type box:Box . " 
                + "?b box:belongsTo <" + String.valueOf(pageIri) + "> . "
                + getDataPropertyUnion()
                + "}";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }
    
    private String getDataPropertyUnion()
    {
        StringBuilder ret = new StringBuilder();
        for (String p : dataObjectProperties)
        {
            if (ret.length() > 0)
                ret.append(" UNION ");
            ret.append("{?b ").append(p).append(" ?s}");
        }
        return ret.toString();
    }
    
}
