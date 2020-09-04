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
public class BoxModelLoader extends ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(BoxModelLoader.class);
    
    private RDFStorage storage;
    private IRI pageIri;
    private Model borderModel;
    private Model attributeModel;
    private RDFPage page;
    

    public BoxModelLoader(RDFStorage storage, IRI pageIri)
    {
        this.storage = storage;
        this.pageIri = pageIri;
    }

    public RDFPage getPage() throws RepositoryException
    {
        if (page == null)
            page = constructPage();
        return page;
    }
    
    private RDFPage constructPage() throws RepositoryException
    {
        Model pageModel = storage.getPageInfo(pageIri);
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
            RDFPage page = new RDFPage(srcURL, info.getId(), info.getDate());
            if (info.getTitle() != null)
                page.setTitle(info.getTitle());
            
            //create the box tree
            Model boxTreeModel = storage.getBoxModelForPage(pageIri);
            Map<IRI, RDFBox> boxes = new LinkedHashMap<IRI, RDFBox>();
            RDFBox root = constructBoxTree(boxTreeModel, boxes); 
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
     * @param model the source model
     * @param boxes the destination map of URIs
     * @return the root box or {@code null} if the provided model does not have a tree structure
     * @throws RepositoryException
     */
    private RDFBox constructBoxTree(Model model, Map<IRI, RDFBox> boxes) throws RepositoryException
    {
        //find all boxes
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFBox box = createBoxFromModel(model, (IRI) res);
                boxes.put((IRI) res, box);
            }
        }
        List<RDFBox> rootBoxes = new ArrayList<RDFBox>(boxes.values());
        //construct the tree
        for (Statement st : model.filter(null, BOX.isChildOf, null))
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
    
    private RDFBox createBoxFromModel(Model model, IRI iri) throws RepositoryException
    {
        RDFBox box = new RDFBox(iri);
        box.setTagName("");
        box.setType(Box.Type.ELEMENT);
        box.setDisplayType(Box.DisplayType.BLOCK);
        int x = 0, y = 0, width = 0, height = 0;
        int vx = 0, vy = 0, vwidth = 0, vheight = 0;
        
        for (Statement st : model.filter(iri, null, null))
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
                    box.setUnderline(((Literal) value).floatValue());
            }
            else if (BOX.lineThrough.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setLineThrough(((Literal) value).floatValue());
            }
            else if (BOX.fontFamily.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setFontFamily(value.stringValue());
            }
            else if (BOX.fontSize.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setFontSize(((Literal) value).floatValue());
            }
            else if (BOX.fontStyle.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setFontStyle(((Literal) value).floatValue());
            }
            else if (BOX.fontWeight.equals(pred)) 
            {
                if (value instanceof Literal)
                    box.setFontWeight(((Literal) value).floatValue());
            }
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    box.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    box.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    box.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
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
                    Map.Entry<String, String> attr = createAttribute(getAttributeModel(), (IRI) value);
                    if (attr != null)
                        box.setAttribute(attr.getKey(), attr.getValue());
                }
            }
        }
        box.setBounds(new Rectangular(x, y, x + width - 1, y + height - 1));
        box.setContentBounds(new Rectangular(x, y, x + width - 1, y + height - 1));
        box.setVisualBounds(new Rectangular(vx, vy, vx + vwidth - 1, vy + vheight - 1));
        
        return box;
    }

    private Model getBorderModel() throws RepositoryException
    {
        if (borderModel == null)
            borderModel = storage.getBorderModelForPage(pageIri);
        return borderModel;
    }
    
    private Model getAttributeModel() throws RepositoryException
    {
        if (attributeModel == null)
            attributeModel = storage.getAttributeModelForPage(pageIri);
        return attributeModel;
    }
    
}
