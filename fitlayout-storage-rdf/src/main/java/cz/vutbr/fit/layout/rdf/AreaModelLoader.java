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

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFBox;
import cz.vutbr.fit.layout.rdf.model.RDFPage;

/**
 * This class implements creating a RDFAreaTree from the RDF models.
 * @author burgetr
 */
public class AreaModelLoader extends ModelLoaderBase implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(AreaModelLoader.class);

    
    public AreaModelLoader()
    {
    }
    
    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo)
            throws RepositoryException
    {
        return constructAreaTree(artifactRepo, artifactIri);
    }

    //================================================================================================
    
    private RDFAreaTree constructAreaTree(RDFArtifactRepository artifactRepo, IRI areaTreeIri) throws RepositoryException
    {
        Model artifactModel = artifactRepo.getStorage().getSubjectModel(areaTreeIri);
        if (artifactModel.size() > 0)
        {
            AreaTreeInfo atreeInfo = new AreaTreeInfo(artifactModel);
            IRI pageIri = getSourcePageIri(artifactModel, areaTreeIri);
            IRI parentIri = getPredicateIriValue(artifactModel, areaTreeIri, FL.hasParentArtifact);
            RDFAreaTree atree = new RDFAreaTree(parentIri, pageIri);
            atreeInfo.applyToAreaTree(atree);
            //load the models
            Model areaModel = getAreaModelForAreaTree(artifactRepo.getStorage(), areaTreeIri);
            Model borderModel = getBorderModelForAreaTree(artifactRepo.getStorage(), areaTreeIri);
            Model tagModel = getTagModelForAreaTree(artifactRepo.getStorage(), areaTreeIri);
            Model tagSupportModel = getTagSupportModelForAreaTree(artifactRepo.getStorage(), areaTreeIri);
            //construct the tree
            Map<IRI, RDFArea> areaUris = new LinkedHashMap<IRI, RDFArea>();
            RDFArea root = constructVisualAreaTree(artifactRepo, atree, areaModel, borderModel, tagModel, tagSupportModel, areaTreeIri, areaUris);
            recursiveUpdateTopologies(root);
            atree.setRoot(root);
            atree.setAreaIris(areaUris);
            return atree;
        }
        else
            return null;
    }
    
    private RDFArea constructVisualAreaTree(RDFArtifactRepository artifactRepo, RDFAreaTree atree,
            Model areaModel, Model borderModel, Model tagModel, Model tagSupportModel,
            IRI areaTreeIri, Map<IRI, RDFArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : areaModel.subjects())
        {
            if (res instanceof IRI)
            {
                RDFArea area = createAreaFromModel(artifactRepo, areaModel, borderModel, tagModel, tagSupportModel, areaTreeIri, (IRI) res);
                area.setAreaTree(atree);
                areas.put((IRI) res, area);
            }
        }
        List<RDFArea> rootAreas = new ArrayList<RDFArea>(areas.values());
        //construct the tree
        for (Statement st : areaModel.filter(null, SEGM.isChildOf, null))
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
    
    private RDFArea createAreaFromModel(RDFArtifactRepository artifactRepo, Model areaModel, Model borderModel, Model tagModel, Model tagSupportModel,
            IRI areaTreeIri, IRI uri) throws RepositoryException
    {
        RDFArea area = new RDFArea(new Rectangular(), uri);
        int x = 0, y = 0, width = 0, height = 0;
        Map<IRI, Float> tagSupport = new HashMap<IRI, Float>(); //tagUri->support
        RDFTextStyle style = new RDFTextStyle();
        
        RDFPage sourcePage = null;
        
        for (Statement st : areaModel.filter(uri, null, null))
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
            else if (SEGM.hasContentLength.equals(pred)) 
            {
                if (value instanceof Literal)
                    style.contentLength = ((Literal) value).intValue();
            }
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    area.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    area.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
                    area.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(borderModel, (IRI) value);
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
                    if (sourcePage == null)
                    {
                        IRI pageIri = getSourcePageIri(areaModel, areaTreeIri);
                        if (pageIri != null)
                            sourcePage = getSourcePage(pageIri, artifactRepo);
                    }
                    if (sourcePage != null)
                    {
                        RDFBox box = sourcePage.findBoxByIri((IRI) value);
                        if (box != null)
                            area.addBox(box);
                    }
                }
            }
            else if (SEGM.hasTag.equals(pred))
            {
                if (value instanceof IRI)
                {
                    if (!tagSupport.containsKey(value))
                    {
                        Tag tag = createTag(tagModel, (IRI) value);
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
                    for (Statement sst : tagSupportModel.filter(tsUri, null, null))
                    {
                        if (SEGM.hasTag.equals(sst.getPredicate()) && sst.getObject() instanceof IRI)
                            tagUri = (IRI) sst.getObject();
                        else if (SEGM.support.equals(sst.getPredicate()) && sst.getObject() instanceof Literal)
                            support = ((Literal) sst.getObject()).floatValue();
                    }
                    if (tagUri != null && support != null)
                    {
                        Tag tag = createTag(tagModel, (IRI) value);
                        if (tag != null)
                            area.addTag(tag, support);
                    }
                }
            }
        }
        area.setTextStyle(style.toTextStyle());
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
    
    private Tag createTag(Model tagModel, IRI tagIri) throws RepositoryException
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
    
    //================================================================================================
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeIri
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getAreaModelForAreaTree(RDFStorage storage, IRI areaTreeIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:Area . "
                + "?s box:documentOrder ?ord . "
                + "?s segm:belongsTo <" + areaTreeIri.stringValue() + "> }"
                + " ORDER BY ?ord";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Gets page border information for the given area tree.
     * @param areaTreeIri
     * @return
     * @throws RepositoryException 
     */
    private Model getBorderModelForAreaTree(RDFStorage storage, IRI areaTreeIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type segm:Area . " 
                + "?b segm:belongsTo <" + areaTreeIri.toString() + "> . "
                + "{?b box:hasTopBorder ?s} UNION {?b box:hasRightBorder ?s} UNION {?b box:hasBottomBorder ?s} UNION {?b box:hasLeftBorder ?s}}";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeIri
     * @return A Model containing the triplets for all tags of the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getTagModelForAreaTree(RDFStorage storage, IRI areaTreeIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:Area . "
                + "?a segm:hasTag ?s . "
                + "?a segm:belongsTo <" + areaTreeIri.stringValue() + "> }";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeIri
     * @return A Model containing the triplets for all tags of the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getTagSupportModelForAreaTree(RDFStorage storage, IRI areaTreeIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:Area . "
                + "?a segm:tagSupport ?s . "
                + "?a segm:belongsTo <" + areaTreeIri.stringValue() + "> }";
        return storage.executeSafeQuery(query);
    }
    
    /**
     * Finds the source page IRI in the page model
     * @param model The page model
     * @param areaTreeIri area tree IRI
     * @return the source page IRI or {@code null} when not defined
     */
    private IRI getSourcePageIri(Model model, IRI areaTreeIri)
    {
        return getPredicateIriValue(model, areaTreeIri, SEGM.hasSourcePage);
    }
    
    /**
     * Loads the source page artifact of the area tree.
     * @param pageIri the source page IRI
     * @param repo the repository used for loading the page artifact.
     * @return the page artifact or {@code null} when not specified or not found
     */
    private RDFPage getSourcePage(IRI pageIri, ArtifactRepository repo)
    {
        RDFPage page = (RDFPage) repo.getArtifact(pageIri);
        return page;
    }
}
