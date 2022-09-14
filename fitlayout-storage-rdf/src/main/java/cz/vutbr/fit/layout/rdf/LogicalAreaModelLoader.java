/**
 * LogicalAreaModelLoader.java
 *
 * Created on 30. 9. 2020, 22:10:33 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
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
    }
    
    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo) throws RepositoryException
    {
        loadTags(artifactRepo); // use the repository tags in the constructed tree
        return constructLogicalAreaTree(artifactRepo, artifactIri);
    }

    //================================================================================================
    
    private LogicalAreaTree constructLogicalAreaTree(RDFArtifactRepository artifactRepo, IRI logicalTreeIri) throws RepositoryException
    {
        Model model = artifactRepo.getStorage().getSubjectModel(logicalTreeIri);
        if (model.size() > 0)
        {
            IRI areaTreeIri = getSourceAreaTreeIri(model, logicalTreeIri);
            RDFAreaTree areaTree = getSourceAreaTree(areaTreeIri, artifactRepo);
            
            DefaultLogicalAreaTree atree = new DefaultLogicalAreaTree(areaTreeIri);
            Map<IRI, RDFLogicalArea> areaUris = new LinkedHashMap<IRI, RDFLogicalArea>();
            RDFLogicalArea root = null;
            final var repo = artifactRepo.getStorage().getRepository();
            try (RepositoryConnection con = repo.getConnection()) {
                root = constructLogicalAreaTree(con, model, areaTree, logicalTreeIri, areaUris);
            }
            if (root != null)
            {
                atree.setRoot(root);
                return atree;
            }
            else
                return null;
        }
        else
            return null;
    }
    
    private RDFLogicalArea constructLogicalAreaTree(RepositoryConnection con, Model model, RDFAreaTree sourceAreaTree, IRI logicalTreeIri, Map<IRI, RDFLogicalArea> areas) throws RepositoryException
    {
        // find area IRIs
        final Set<Resource> areaIris = new HashSet<>();
        try (RepositoryResult<Statement> result = con.getStatements(null, RDF.TYPE, SEGM.LogicalArea, logicalTreeIri)) {
            for (Statement st : result)
                areaIris.add(st.getSubject());
        }
        
        //find all areas
        for (Resource res : areaIris)
        {
            if (res instanceof IRI)
            {
                RDFLogicalArea area = createLogicalArea(con, logicalTreeIri, (IRI) res, model, sourceAreaTree);
                areas.put((IRI) res, area);
            }
        }
        
        //construct the tree
        List<RDFLogicalArea> rootAreas = new ArrayList<RDFLogicalArea>(areas.values());
        try (RepositoryResult<Statement> result = con.getStatements(null, SEGM.isSubordinateTo, null, logicalTreeIri)) {
            for (Statement st : result)
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
        }
        if (rootAreas.size() == 1)
            return rootAreas.get(0);
        else
        {
            log.error("Strange number of root logical areas: {}", rootAreas.toString());
            return null; //strange number of root nodes
        }
        
    }
    
    private RDFLogicalArea createLogicalArea(RepositoryConnection con, IRI treeIri, IRI iri,
            Model artifactModel, RDFAreaTree sourceAreaTree) throws RepositoryException
    {
        RDFLogicalArea area = new RDFLogicalArea(iri);
        
        try (RepositoryResult<Statement> result = con.getStatements(iri, null, null, treeIri)) {
            for (Statement st : result)
            {
                final IRI pred = st.getPredicate();
                final Value value = st.getObject();
                
                if (SEGM.text.equals(pred)) 
                {
                    area.setText(value.stringValue());
                }
                else if (SEGM.hasTag.equals(pred))
                {
                    if (value instanceof IRI)
                    {
                        Tag tag = getTag((IRI) value);
                        if (tag != null)
                            area.setMainTag(tag);
                    }
                }
                else if (SEGM.containsArea.equals(pred))
                {
                    if (value instanceof IRI)
                    {
                        if (sourceAreaTree != null)
                        {
                            Area a = sourceAreaTree.findAreaByIri((IRI) value);
                            if (a != null)
                                area.addArea(a);
                        }
                    }
                }
            }
        }
        
        return area;
    }

}
