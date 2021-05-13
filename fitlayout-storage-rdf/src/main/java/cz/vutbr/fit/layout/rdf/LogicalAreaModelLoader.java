/**
 * LogicalAreaModelLoader.java
 *
 * Created on 30. 9. 2020, 22:10:33 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultLogicalAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFLogicalArea;

/**
 * 
 * @author burgetr
 */
public class LogicalAreaModelLoader extends ModelLoaderBase implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(LogicalAreaModelLoader.class);
    
    public LogicalAreaModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo) throws RepositoryException
    {
        return constructLogicalAreaTree(artifactRepo, artifactIri);
    }

    //================================================================================================
    
    private LogicalAreaTree constructLogicalAreaTree(RDFArtifactRepository artifactRepo, IRI logicalTreeIri) throws RepositoryException
    {
        Model model = getLogicalAreaModelForAreaTree(artifactRepo, logicalTreeIri);
        if (model.size() > 0)
        {
            IRI areaTreeIri = getSourceAreaTreeIri(model, logicalTreeIri);
            DefaultLogicalAreaTree atree = new DefaultLogicalAreaTree(areaTreeIri);
            Map<IRI, RDFLogicalArea> areaUris = new LinkedHashMap<IRI, RDFLogicalArea>();
            RDFLogicalArea root = constructLogicalAreaTree(artifactRepo, model, logicalTreeIri, areaUris);
            atree.setRoot(root);
            return atree;
        }
        else
            return null;
    }
    
    private RDFLogicalArea constructLogicalAreaTree(RDFArtifactRepository artifactRepo, Model model, IRI logicalTreeIri, Map<IRI, RDFLogicalArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFLogicalArea area = createLogicalAreaFromModel(artifactRepo, model, logicalTreeIri, (IRI) res);
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
    
    private RDFLogicalArea createLogicalAreaFromModel(RDFArtifactRepository artifactRepo, Model model, IRI treeIri, IRI iri) throws RepositoryException
    {
        RDFLogicalArea area = new RDFLogicalArea(iri);
        
        Model tagInfoModel = null;
        RDFAreaTree areaTree = null;
        
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
                    if (tagInfoModel == null)
                        tagInfoModel = getTagModelForAreaTree(artifactRepo, treeIri);
                    Tag tag = createTag(tagInfoModel, (IRI) value);
                    if (tag != null)
                        area.setMainTag(tag);
                }
            }
            else if (SEGM.containsArea.equals(pred))
            {
                if (value instanceof IRI)
                {
                    if (areaTree == null)
                    {
                        IRI areaTreeIri = getSourceAreaTreeIri(model, treeIri);
                        if (areaTreeIri != null)
                            areaTree = getSourceAreaTree(areaTreeIri, artifactRepo);
                    }
                    if (areaTree != null)
                    {
                        Area a = areaTree.findAreaByIri((IRI) value);
                        if (a != null)
                            area.addArea(a);
                    }
                }
            }
        }
        
        return area;
    }

    //================================================================================================
    
    /**
     * Obtains the model of logical areas for the given area tree.
     * @param logicalTreeIri
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getLogicalAreaModelForAreaTree(RDFArtifactRepository artifactRepo, IRI logicalTreeIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:LogicalArea . "
                + "?s box:documentOrder ?ord . "
                + "?s segm:belongsTo <" + logicalTreeIri.stringValue() + "> }" //TODO is belongsTo correct?
                + " ORDER BY ?ord";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeIri
     * @return A Model containing the triplets for all tags of the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getTagModelForAreaTree(RDFArtifactRepository artifactRepo, IRI areaTreeIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:LogicalArea . "
                + "?a segm:hasTag ?s . "
                + "?a segm:belongsTo <" + areaTreeIri.stringValue() + "> }";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }

}
