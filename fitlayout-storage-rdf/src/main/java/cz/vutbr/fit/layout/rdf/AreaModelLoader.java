/**
 * AreaModelLoader.java
 *
 * Created on 17. 1. 2016, 13:38:37 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultLogicalAreaTree;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFBox;
import cz.vutbr.fit.layout.rdf.model.RDFLogicalArea;
import cz.vutbr.fit.layout.rdf.model.RDFPage;

/**
 * This class implements creating a RDFAreaTree from the RDF models.
 * @author burgetr
 */
public class AreaModelLoader extends ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(AreaModelLoader.class);

    private RDFStorage storage;
    private IRI areaTreeIri;
    private RDFPage page;
    
    private Model borderModel;
    private Model tagInfoModel;
    private Model tagSupportModel;
    
    private RDFAreaTree areaTree;
    private LogicalAreaTree logicalAreaTree;
    
    public AreaModelLoader(RDFStorage storage, IRI areaTreeIri, RDFPage srcPage)
    {
        this.storage = storage;
        this.areaTreeIri = areaTreeIri;
        this.page = srcPage;
    }
    
    public RDFAreaTree getAreaTree() throws RepositoryException
    {
        if (areaTree == null)
            areaTree = constructAreaTree();
        return areaTree;
    }

    public LogicalAreaTree getLogicalAreaTree() throws RepositoryException
    {
        if (areaTree != null)
        {
            if (logicalAreaTree == null)
                logicalAreaTree = constructLogicalAreaTree();
            return logicalAreaTree;
        }
        else
            return null;
    }
    
    //================================================================================================
    
    private RDFAreaTree constructAreaTree() throws RepositoryException
    {
        Model model = storage.getAreaModelForAreaTree(areaTreeIri);
        if (model.size() > 0)
        {
            RDFAreaTree atree = new RDFAreaTree(page, areaTreeIri);
            Map<IRI, RDFArea> areaUris = new LinkedHashMap<IRI, RDFArea>();
            RDFArea root = constructVisualAreaTree(model, areaUris);
            recursiveUpdateTopologies(root);
            atree.setRoot(root);
            atree.setAreaIris(areaUris);
            return atree;
        }
        else
            return null;
    }
    
    private RDFArea constructVisualAreaTree(Model model, Map<IRI, RDFArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFArea area = createAreaFromModel(model, (IRI) res);
                areas.put((IRI) res, area);
            }
        }
        List<RDFArea> rootAreas = new ArrayList<RDFArea>(areas.values());
        //construct the tree
        for (Statement st : model.filter(null, SEGM.isChildOf, null))
        {
            if (st.getSubject() instanceof IRI && st.getObject() instanceof IRI)
            {
                RDFArea parent = areas.get(st.getObject());
                RDFArea child = areas.get(st.getSubject());
                if (parent != null && child != null)
                {
                    parent.appendChild(child);
                    rootAreas.remove(child);
                }
            }
        }
        if (rootAreas.size() == 1)
            return rootAreas.get(0);
        else
        {
            log.error("Strange number of root areas: {}", rootAreas.toString());
            return null; //strange number of root nodes
        }
    }
    
    private RDFArea createAreaFromModel(Model model, IRI uri) throws RepositoryException
    {
        RDFArea area = new RDFArea(new Rectangular(), uri);
        int x = 0, y = 0, width = 0, height = 0;
        Map<IRI, Float> tagSupport = new HashMap<IRI, Float>(); //tagUri->support
        
        for (Statement st : model.filter(uri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (RDFS.LABEL.equals(pred))
            {
                String name = value.stringValue();
                area.setName(name);
            }
            else if (BOX.documentOrder.equals(pred))
            {
                if (value instanceof Literal)
                    area.setDocumentOrder(((Literal) value).intValue());
            }
            else if (BOX.backgroundColor.equals(pred)) 
            {
                String bgColor = value.stringValue();
                area.setBackgroundColor( Serialization.decodeHexColor( bgColor ) );
            }
            else if (BOX.underline.equals(pred)) 
            {
                if (value instanceof Literal)
                    area.setUnderline(((Literal) value).floatValue());
            }
            else if (BOX.lineThrough.equals(pred)) 
            {
                if (value instanceof Literal)
                    area.setLineThrough(((Literal) value).floatValue());
            }
            else if (BOX.fontSize.equals(pred)) 
            {
                if (value instanceof Literal)
                    area.setFontSize(((Literal) value).floatValue());
            }
            else if (BOX.fontStyle.equals(pred)) 
            {
                if (value instanceof Literal)
                    area.setFontStyle(((Literal) value).floatValue());
            }
            else if (BOX.fontWeight.equals(pred)) 
            {
                if (value instanceof Literal)
                    area.setFontWeight(((Literal) value).floatValue());
            }
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    area.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    area.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    area.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(getBorderModel(), (IRI) value);
                    area.setBorderStyle(Side.TOP, border);
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
            else if (SEGM.containsBox.equals(pred))
            {
                if (value instanceof IRI)
                {
                    RDFBox box = page.findBoxByIri((IRI) value);
                    if (box != null)
                        area.addBox(box);
                }
            }
            else if (SEGM.hasTag.equals(pred))
            {
                if (value instanceof IRI)
                {
                    if (!tagSupport.containsKey(value))
                    {
                        Tag tag = createTag((IRI) value);
                        if (tag != null)
                            area.addTag(tag, 1.0f); //spport is unkwnown (yet)
                    }
                }
            }
            else if (SEGM.tagSupport.equals(pred))
            {
                if (value instanceof IRI)
                {
                    IRI tsUri = (IRI) value;
                    IRI tagUri = null;
                    Float support = null;
                    for (Statement sst : getTagSupportModel().filter(tsUri, null, null))
                    {
                        if (SEGM.hasTag.equals(sst.getPredicate()) && sst.getObject() instanceof IRI)
                            tagUri = (IRI) sst.getObject();
                        else if (SEGM.support.equals(sst.getPredicate()) && sst.getObject() instanceof Literal)
                            support = ((Literal) sst.getObject()).floatValue();
                    }
                    if (tagUri != null && support != null)
                    {
                        Tag tag = createTag((IRI) value);
                        if (tag != null)
                            area.addTag(tag, support);
                    }
                }
            }
        }
        area.setBounds(new Rectangular(x, y, x + width - 1, y + height - 1));
        area.sortBoxes();
        
        return area;
    }
    
    private void recursiveUpdateTopologies(Area root)
    {
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveUpdateTopologies(root.getChildAt(i));
        root.updateTopologies();
    }
    
    //================================================================================================
    
    private LogicalAreaTree constructLogicalAreaTree() throws RepositoryException
    {
        Model model = storage.getLogicalAreaModelForAreaTree(areaTreeIri);
        if (model.size() > 0)
        {
            DefaultLogicalAreaTree atree = new DefaultLogicalAreaTree(areaTree);
            Map<IRI, RDFLogicalArea> areaUris = new LinkedHashMap<IRI, RDFLogicalArea>();
            RDFLogicalArea root = constructLogicalAreaTree(model, areaUris);
            atree.setRoot(root);
            return atree;
        }
        else
            return null;
    }
    
    private RDFLogicalArea constructLogicalAreaTree(Model model, Map<IRI, RDFLogicalArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFLogicalArea area = createLogicalAreaFromModel(model, (IRI) res);
                areas.put((IRI) res, area);
            }
        }
        List<RDFLogicalArea> rootAreas = new ArrayList<RDFLogicalArea>(areas.values());
        //construct the tree
        for (Statement st : model.filter(null, SEGM.isSubordinateTo, null))
        {
            if (st.getSubject() instanceof IRI && st.getObject() instanceof IRI)
            {
                RDFLogicalArea parent = areas.get(st.getObject());
                RDFLogicalArea child = areas.get(st.getSubject());
                if (parent != null && child != null)
                {
                    parent.appendChild(child);
                    rootAreas.remove(child);
                }
            }
        }
        if (rootAreas.size() == 1)
            return rootAreas.get(0);
        else
        {
            log.error("Strange number of root logical areas: {}", rootAreas.toString());
            return null; //strange number of root nodes
        }
        
    }
    
    private RDFLogicalArea createLogicalAreaFromModel(Model model, IRI iri) throws RepositoryException
    {
        RDFLogicalArea area = new RDFLogicalArea(iri);
        
        for (Statement st : model.filter(iri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (SEGM.hasText.equals(pred)) 
            {
                area.setText(value.stringValue());
            }
            else if (SEGM.hasTag.equals(pred))
            {
                if (value instanceof IRI)
                {
                    Tag tag = createTag((IRI) value);
                    if (tag != null)
                        area.setMainTag(tag);
                }
            }
            else if (SEGM.containsArea.equals(pred))
            {
                if (value instanceof IRI)
                {
                    Area a = areaTree.findAreaByIri((IRI) value);
                    if (a != null)
                        area.addArea(a);
                }
            }
        }
        
        return area;
    }
    
    //================================================================================================
    
    private Tag createTag(IRI tagIri) throws RepositoryException
    {
        String name = null;
        String type = null;
        for (Statement st : getTagInfoModel().filter(tagIri, null, null))
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
    
    //================================================================================================
    
    private Model getBorderModel() throws RepositoryException
    {
        if (borderModel == null)
            borderModel = storage.getBorderModelForAreaTree(areaTreeIri);
        return borderModel;
    }
    
    private Model getTagInfoModel() throws RepositoryException
    {
        if (tagInfoModel == null)
            tagInfoModel = storage.getTagModelForAreaTree(areaTreeIri);
        return tagInfoModel;
    }

    private Model getTagSupportModel() throws RepositoryException
    {
        if (tagSupportModel == null)
            tagSupportModel = storage.getTagSupportModelForAreaTree(areaTreeIri);
        return tagSupportModel;
    }

}
