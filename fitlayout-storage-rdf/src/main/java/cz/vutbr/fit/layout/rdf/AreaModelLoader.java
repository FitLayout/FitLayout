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

    private static final String[] dataObjectProperties = new String[] { 
            "box:hasTopBorder",
            "box:hasBottomBorder",
            "box:hasLeftBorder",
            "box:hasRightBorder",
            "box:hasAttribute",
            "box:bounds",
            "segm:hasTag",
            "segm:tagSupport"
    };
    
    public AreaModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
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
            AreaTreeInfo atreeInfo = new AreaTreeInfo(artifactModel, areaTreeIri);
            IRI pageIri = getSourcePageIri(artifactModel, areaTreeIri);
            IRI parentIri = getPredicateIriValue(artifactModel, areaTreeIri, FL.hasParentArtifact);
            RDFAreaTree atree = new RDFAreaTree(parentIri, pageIri);
            atreeInfo.applyToAreaTree(atree);
            //load the models
            Model areaModel = getAreaModelForAreaTree(artifactRepo, areaTreeIri);
            Model dataModel = getAreaDataModelForAreaTree(artifactRepo, areaTreeIri);
            //load the source page
            RDFPage sourcePage = null;
            if (pageIri != null)
                sourcePage = getSourcePage(pageIri, artifactRepo);
            //construct the tree
            Map<IRI, RDFArea> areaUris = new LinkedHashMap<IRI, RDFArea>();
            RDFArea root = constructVisualAreaTree(artifactRepo, sourcePage, atree, areaModel, dataModel, areaTreeIri, areaUris);
            recursiveUpdateTopologies(root);
            atree.setRoot(root);
            atree.setAreaIris(areaUris);
            return atree;
        }
        else
            return null;
    }
    
    private RDFArea constructVisualAreaTree(RDFArtifactRepository artifactRepo, RDFPage sourcePage, RDFAreaTree atree,
            Model areaModel, Model dataModel,
            IRI areaTreeIri, Map<IRI, RDFArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : areaModel.subjects())
        {
            if (res instanceof IRI)
            {
                RDFArea area = createAreaFromModel(artifactRepo, sourcePage, areaModel, dataModel, areaTreeIri, (IRI) res);
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
    
    private RDFArea createAreaFromModel(RDFArtifactRepository artifactRepo, RDFPage sourcePage, Model areaModel, Model dataModel,
            IRI areaTreeIri, IRI uri) throws RepositoryException
    {
        RDFArea area = new RDFArea(new Rectangular(), uri);
        area.setId(getIriFactory().decodeAreaId(uri));
        Map<IRI, Float> tagSupport = new HashMap<>(); //tagUri->support
        RDFTextStyle style = new RDFTextStyle();
        
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
            else if (SEGM.contentLength.equals(pred)) 
            {
                if (value instanceof Literal)
                    style.contentLength = ((Literal) value).intValue();
            }
            else if (BOX.hasBottomBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    area.setBorderStyle(Side.BOTTOM, border);
                }
            }
            else if (BOX.hasLeftBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    area.setBorderStyle(Side.LEFT, border);
                }
            }
            else if (BOX.hasRightBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    area.setBorderStyle(Side.RIGHT, border);
                }
            }
            else if (BOX.hasTopBorder.equals(pred)) 
            {
                if (value instanceof IRI)
                {
                    Border border = createBorder(dataModel, (IRI) value);
                    area.setBorderStyle(Side.TOP, border);
                }
            }
            else if (BOX.bounds.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Rectangular rect = createBounds(dataModel, (IRI) value);
                    if (rect != null)
                        area.setBounds(rect);
                }
            }
            else if (SEGM.containsBox.equals(pred))
            {
                if (value instanceof IRI)
                {
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
                        Tag tag = createTag(dataModel, (IRI) value);
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
                    for (Statement sst : dataModel.filter(tsUri, null, null))
                    {
                        if (SEGM.hasTag.equals(sst.getPredicate()) && sst.getObject() instanceof IRI)
                            tagUri = (IRI) sst.getObject();
                        else if (SEGM.support.equals(sst.getPredicate()) && sst.getObject() instanceof Literal)
                            support = ((Literal) sst.getObject()).floatValue();
                    }
                    if (tagUri != null && support != null)
                    {
                        Tag tag = createTag(dataModel, (IRI) value);
                        if (tag != null)
                            area.addTag(tag, support);
                    }
                }
            }
        }
        area.setTextStyle(style.toTextStyle());
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
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param artifactRepo the repository to query 
     * @param areaTreeIri the area tree IRI
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getAreaModelForAreaTree(RDFArtifactRepository artifactRepo, IRI areaTreeIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:Area . "
                + "?s box:documentOrder ?ord . "
                + "?s segm:belongsTo <" + areaTreeIri.stringValue() + "> }"
                + " ORDER BY ?ord";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }
    
    /**
     * Gets the model of additional object properties of the areas. It contains the data about the
     * bounds, borders, tags and other object properties.
     * @param artifactRepo the repository to query 
     * @param areaTreeIri the area tree IRI
     * @return The created model
     * @throws RepositoryException 
     */
    private Model getAreaDataModelForAreaTree(RDFArtifactRepository artifactRepo, IRI areaTreeIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:Area . "
                + "?a segm:belongsTo <" + areaTreeIri.stringValue() + "> . "
                + getDataPropertyUnion(dataObjectProperties)
                + "}";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }
    
}
