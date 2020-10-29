/**
 * BoxModelLoader.java
 *
 * Created on 13. 1. 2016, 23:49:14 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
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
import cz.vutbr.fit.layout.ontology.FL;
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
    

    public BoxModelLoader()
    {
    }

    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo)
            throws RepositoryException
    {
        return constructPage(artifactRepo.getStorage(), artifactIri);
    }

    private RDFPage constructPage(RDFStorage storage, IRI pageIri) throws RepositoryException
    {
        Model pageModel = storage.getSubjectModel(pageIri);
        if (pageModel.size() > 0)
        {
            //create the page
            PageInfo info = new PageInfo(pageModel);
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
            RDFPage page = new RDFPage(srcURL, info.getDate());
            page.setIri(info.getId());
            page.setParentIri(getPredicateIriValue(pageModel, pageIri, FL.hasParentArtifact));
            if (info.getTitle() != null)
                page.setTitle(info.getTitle());
            
            //load the models
            Model boxTreeModel = getBoxModelForPage(storage, pageIri);
            Model borderModel = getBorderModelForPage(storage, pageIri);
            Model attributeModel = getAttributeModelForPage(storage, pageIri);
            //create the box tree
            Map<IRI, RDFBox> boxes = new LinkedHashMap<IRI, RDFBox>();
            RDFBox root = constructBoxTree(storage, boxTreeModel, borderModel, attributeModel, pageIri, boxes); 
            page.setRoot(root);
            page.setBoxIris(boxes);
            page.setWidth(root.getWidth());
            page.setHeight(root.getHeight());
            
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
    private RDFBox constructBoxTree(RDFStorage storage, Model boxTreeModel, Model borderModel, Model attributeModel,
            IRI pageIri, Map<IRI, RDFBox> boxes) throws RepositoryException
    {
        //find all boxes
        for (Resource res : boxTreeModel.subjects())
        {
            if (res instanceof IRI)
            {
                RDFBox box = createBoxFromModel(storage, boxTreeModel, borderModel, attributeModel, pageIri, (IRI) res);
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
    
    private RDFBox createBoxFromModel(RDFStorage storage, Model boxTreeModel, Model borderModel, Model attributeModel, 
            IRI pageIri, IRI boxIri) throws RepositoryException
    {
        RDFBox box = new RDFBox(boxIri);
        box.setTagName("");
        box.setType(Box.Type.ELEMENT);
        box.setDisplayType(Box.DisplayType.BLOCK);
        int x = 0, y = 0, width = 0, height = 0;
        int vx = 0, vy = 0, vwidth = 0, vheight = 0;
        RDFTextStyle style = new RDFTextStyle();
        
        for (Statement st : boxTreeModel.filter(boxIri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (BOX.documentOrder.equals(pred))
            {
                if (value instanceof Literal)
                    box.setDocumentOrder(((Literal) value).intValue());
            }
            else if (BOX.backgroundColor.equals(pred)) 
            {
                String bgColor = value.stringValue();
                //bgColor = bgColor.substring(1,bgColor.length());
                box.setBackgroundColor( Serialization.decodeHexColor( bgColor ) );
            }
            else if (BOX.backgroundImagePosition.equals(pred)) 
            {
            }
            else if (BOX.backgroundImageUrl.equals(pred)) 
            {
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
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    box.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    box.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    box.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    box.setBorderStyle(Side.TOP, border);
                }
            }
            else if (BOX.hasText.equals(pred)) 
            {
                if (box.getType() != Type.REPLACED_CONTENT) //once it is a replaced box, do not change it back to text box
                    box.setType(Type.TEXT_CONTENT);
                box.setText(value.stringValue());
            }
            else if (BOX.containsImage.equals(pred))
            {
                box.setType(Type.REPLACED_CONTENT);
                if (value instanceof IRI)
                {
                    RDFContentImage obj = new RDFContentImage((IRI) value);
                    Value val = storage.getPropertyValue((IRI) value, BOX.imageUrl);
                    if (val != null && val instanceof Literal)
                    {
                        try {
                            obj.setUrl(((Literal) val).stringValue());
                        } catch (MalformedURLException e) {
                            log.error(e.getMessage());
                        }
                    }   
                    box.setContentObject(obj);
                }
            }
            else if (BOX.containsObject.equals(pred))
            {
                box.setType(Type.REPLACED_CONTENT);
                if (value instanceof IRI)
                {
                    RDFContentObject obj = new RDFContentObject((IRI) value);
                    box.setContentObject(obj);
                }
            }
            else if (BOX.height.equals(pred)) 
            {
                if (value instanceof Literal)
                    height = ((Literal) value).intValue();
            }
            else if (BOX.width.equals(pred)) 
            {
                if (value instanceof Literal)
                    width = ((Literal) value).intValue();
            }
            else if (BOX.positionX.equals(pred)) 
            {
                if (value instanceof Literal)
                    x = ((Literal) value).intValue();
            }   
            else if (BOX.positionY.equals(pred)) 
            {
                if (value instanceof Literal)
                    y = ((Literal) value).intValue();
            }
            else if (BOX.visualHeight.equals(pred)) 
            {
                if (value instanceof Literal)
                    vheight = ((Literal) value).intValue();
            }
            else if (BOX.visualWidth.equals(pred)) 
            {
                if (value instanceof Literal)
                    vwidth = ((Literal) value).intValue();
            }
            else if (BOX.visualX.equals(pred)) 
            {
                if (value instanceof Literal)
                    vx = ((Literal) value).intValue();
            }   
            else if (BOX.visualY.equals(pred)) 
            {
                if (value instanceof Literal)
                    vy = ((Literal) value).intValue();
            }
            else if (BOX.htmlTagName.equals(pred)) 
            {
                box.setTagName(value.stringValue());
            }
            else if (BOX.hasAttribute.equals(pred))
            {
                if (value instanceof IRI)
                {
                    Map.Entry<String, String> attr = createAttribute(attributeModel, (IRI) value);
                    if (attr != null)
                        box.setAttribute(attr.getKey(), attr.getValue());
                }
            }
        }
        style.contentLength = box.getText().length();
        box.setTextStyle(style.toTextStyle());
        box.setBounds(new Rectangular(x, y, x + width - 1, y + height - 1));
        box.setContentBounds(new Rectangular(x, y, x + width - 1, y + height - 1));
        box.setVisualBounds(new Rectangular(vx, vy, vx + vwidth - 1, vy + vheight - 1));
        
        return box;
    }

    /**
     * Gets page box model from the unique page ID.
     * @param storage 
     * @param pageIri
     * @return
     * @throws RepositoryException 
     */
    private Model getBoxModelForPage(RDFStorage storage, IRI pageIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type box:Box . "
                + "?s box:documentOrder ?ord . "
                + "?s box:belongsTo <" + pageIri.toString() + ">}"
                + " ORDER BY ?ord";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Gets page box model from the unique page ID.
     * @param storage 
     * @param pageIri
     * @return
     * @throws RepositoryException 
     */
    private Model getBorderModelForPage(RDFStorage storage, IRI pageIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type box:Box . " 
                + "?b box:belongsTo <" + pageIri.toString() + "> . "
                + "{?b box:hasTopBorder ?s} UNION {?b box:hasRightBorder ?s} UNION {?b box:hasBottomBorder ?s} UNION {?b box:hasLeftBorder ?s}}";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Gets page attribute model from the unique page ID.
     * @param storage 
     * @param pageIri
     * @return
     * @throws RepositoryException 
     */
    private Model getAttributeModelForPage(RDFStorage storage, IRI pageIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type box:Box . " 
                + "?b box:belongsTo <" + pageIri.toString() + "> . "
                + "?b box:hasAttribute ?s}";
        return storage.executeSafeQuery(query);
    }
}
