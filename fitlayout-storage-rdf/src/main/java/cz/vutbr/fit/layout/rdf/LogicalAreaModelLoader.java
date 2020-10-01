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
import cz.vutbr.fit.layout.impl.DefaultTag;
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
public class LogicalAreaModelLoader implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(LogicalAreaModelLoader.class);
    
    
    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFStorage storage,
            Artifact parentArtifact) throws RepositoryException
    {
        return constructLogicalAreaTree(storage, (RDFAreaTree) parentArtifact, artifactIri);
    }

    //================================================================================================
    
    private LogicalAreaTree constructLogicalAreaTree(RDFStorage storage, RDFAreaTree areaTree, IRI treeIri) throws RepositoryException
    {
        Model model = getLogicalAreaModelForAreaTree(storage, areaTree.getIri());
        if (model.size() > 0)
        {
            DefaultLogicalAreaTree atree = new DefaultLogicalAreaTree(areaTree.getIri());
            Map<IRI, RDFLogicalArea> areaUris = new LinkedHashMap<IRI, RDFLogicalArea>();
            RDFLogicalArea root = constructLogicalAreaTree(storage, model, areaTree, treeIri, areaUris);
            atree.setRoot(root);
            return atree;
        }
        else
            return null;
    }
    
    private RDFLogicalArea constructLogicalAreaTree(RDFStorage storage, Model model, RDFAreaTree areaTree, IRI treeIri, Map<IRI, RDFLogicalArea> areas) throws RepositoryException
    {
        //find all areas
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFLogicalArea area = createLogicalAreaFromModel(storage, model, areaTree, treeIri, (IRI) res);
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
    
    private RDFLogicalArea createLogicalAreaFromModel(RDFStorage storage, Model model, RDFAreaTree areaTree, IRI treeIri, IRI iri) throws RepositoryException
    {
        RDFLogicalArea area = new RDFLogicalArea(iri);
        
        Model tagInfoModel = null;
        
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
                        tagInfoModel = getTagModelForAreaTree(storage, treeIri);
                    Tag tag = createTag(tagInfoModel, (IRI) value);
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
    
    /**
     * Obtains the model of logical areas for the given area tree.
     * @param areaTreeIri
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getLogicalAreaModelForAreaTree(RDFStorage storage, IRI areaTreeIri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:LogicalArea . "
                + "?s box:documentOrder ?ord . "
                + "?s segm:belongsTo <" + areaTreeIri.stringValue() + "> }"
                + " ORDER BY ?ord";
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
                + "?a rdf:type segm:LogicalArea . "
                + "?a segm:hasTag ?s . "
                + "?a segm:belongsTo <" + areaTreeIri.stringValue() + "> }";
        return storage.executeSafeQuery(query);
    }

}
