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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Artifact;
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
    
    private int next_id;

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
        next_id = 0;
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
            
            //create the box tree
            Map<IRI, RDFBox> boxes = new LinkedHashMap<IRI, RDFBox>();
            RDFBox root = null;
            final var repo = artifactRepo.getStorage().getRepository();
            try (RepositoryConnection con = repo.getConnection()) {
                root = constructBoxTree(con, pageIri,
                        boxes, page.getAdditionalStatements());
            }
            if (root != null)
            {
                checkChildOrderValues(root);
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
    private RDFBox constructBoxTree(RepositoryConnection con,
            IRI pageIri, Map<IRI, RDFBox> boxes, Collection<Statement> additionalStatements) throws RepositoryException
    {
        // find box IRIs
        final Set<Resource> boxIris = new HashSet<>();
        try (RepositoryResult<Statement> result = con.getStatements(null, RDF.TYPE, BOX.Box, pageIri)) {
            for (Statement st : result)
                boxIris.add(st.getSubject());
        }
        
        // create boxes
        List<RDFBox> boxList = new ArrayList<>(boxIris.size());
        for (Resource res : boxIris)
        {
            if (res instanceof IRI)
            {
                final RDFBox box = createBox(con, pageIri, (IRI) res, additionalStatements);
                boxList.add(box);
            }
        }
        
        // sort and put to map
        boxList.sort(new Comparator<RDFBox>() {
            @Override
            public int compare(RDFBox o1, RDFBox o2)
            {
                return o1.getDocumentOrder() - o2.getDocumentOrder();
            }
        });
        for (RDFBox box : boxList)
            boxes.put(box.getIri(), box);
        
        //construct the tree
        List<RDFBox> rootBoxes = new ArrayList<RDFBox>(boxList);
        try (RepositoryResult<Statement> result = con.getStatements(null, BOX.isChildOf, null, pageIri)) {
            for (Statement st : result)
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
        }
        if (rootBoxes.size() == 1)
            return rootBoxes.get(0);
        else
        {
            log.error("Strange number of root boxes: {}", rootBoxes.toString());
            return null; //strange number of root nodes
        }
    }
    
    private RDFBox createBox(RepositoryConnection con, IRI pageIri, IRI boxIri, Collection<Statement> additionalStatements) throws RepositoryException
    {
        RDFBox box = new RDFBox(boxIri);
        box.setId(next_id++);
        box.setOrder(-1);
        box.setTagName("");
        box.setType(Box.Type.ELEMENT);
        box.setDisplayType(Box.DisplayType.BLOCK);
        
        RDFTextStyle style = new RDFTextStyle();
        try (RepositoryResult<Statement> result = con.getStatements(boxIri, null, null, pageIri)) {
            for (Statement st : result)
            {
                final IRI pred = st.getPredicate();
                final Value value = st.getObject();
                
                if (processContentRectProperty(con, pred, value, box) || processStyleProperty(pred, value, style))
                {
                    // sucessfully processed
                }
                else if (BOX.documentOrder.equals(pred))
                {
                    if (value instanceof Literal)
                        box.setOrder(((Literal) value).intValue());
                }
                else if (BOX.visible.equals(pred)) 
                {
                    if (value instanceof Literal)
                        box.setVisible(((Literal) value).booleanValue());
                }
                else if (BOX.hasBackgroundImage.equals(pred)) 
                {
                    if (value instanceof IRI)
                    {
                        final RDFContentImage image = loadImage(con, (IRI) value);
                        box.setBackgroundImagePng(image.getPngData());
                    }
                }
                else if (BOX.color.equals(pred)) 
                {
                    box.setColor(Serialization.decodeHexColor(value.stringValue()));
                }
                else if (BOX.fontFamily.equals(pred)) 
                {
                    if (value instanceof Literal)
                        box.setFontFamily(value.stringValue());
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
                        final Value valueType = getPropertyValue(con, (IRI) value, RDF.TYPE);
                        if (BOX.Image.equals(valueType)) // treat images in a special way
                        {
                            final RDFContentImage obj = loadImage(con, (IRI) value);
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
                        final Rectangular rect = createBounds(con, (IRI) value);
                        if (rect != null)
                            box.setBounds(rect);
                    }
                }
                else if (BOX.visualBounds.equals(pred))
                {
                    if (value instanceof IRI)
                    {
                        final Rectangular rect = createBounds(con, (IRI) value);
                        if (rect != null)
                            box.setVisualBounds(rect);
                    }
                }
                else if (BOX.contentBounds.equals(pred))
                {
                    if (value instanceof IRI)
                    {
                        final Rectangular rect = createBounds(con, (IRI) value);
                        if (rect != null)
                            box.setContentBounds(rect);
                    }
                }
                else if (BOX.sourceXPath.equals(pred)) 
                {
                    box.setSourceNodeId(value.stringValue());
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
                        Map.Entry<String, String> attr = createAttribute(con, (IRI) value);
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
    private RDFContentImage loadImage(RepositoryConnection con, final IRI imageIri)
    {
        RDFContentImage obj = new RDFContentImage(imageIri);
        Value urlVal = getPropertyValue(con, imageIri, BOX.imageUrl);
        if (urlVal != null && urlVal instanceof Literal)
        {
            try {
                obj.setUrl(((Literal) urlVal).stringValue());
            } catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
        Value dataVal = getPropertyValue(con, imageIri, BOX.imageData);
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
    
}
