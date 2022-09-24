/**
 * AreaModelLoader.java
 *
 * Created on 17. 1. 2016, 13:38:37 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
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

    private int next_id;
    
    public AreaModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo)
            throws RepositoryException
    {
        loadTags(artifactRepo); // use the repository tags in the constructed tree
        return constructAreaTree(artifactRepo, artifactIri);
    }

    //================================================================================================
    
    private RDFAreaTree constructAreaTree(RDFArtifactRepository artifactRepo, IRI areaTreeIri) throws RepositoryException
    {
        next_id = 0;
        Model artifactModel = artifactRepo.getStorage().getSubjectModel(areaTreeIri);
        if (artifactModel.size() > 0)
        {
            AreaTreeInfo atreeInfo = new AreaTreeInfo(artifactModel, areaTreeIri);
            IRI pageIri = getSourcePageIri(artifactModel, areaTreeIri);
            IRI parentIri = getPredicateIriValue(artifactModel, areaTreeIri, FL.hasParentArtifact);
            RDFAreaTree atree = new RDFAreaTree(parentIri, pageIri);
            atreeInfo.applyToAreaTree(atree);
            //load the source page
            RDFPage sourcePage = null;
            if (pageIri != null)
                sourcePage = getSourcePage(pageIri, artifactRepo);
            //construct the tree
            Map<IRI, RDFArea> areaUris = new LinkedHashMap<IRI, RDFArea>();
            RDFArea root;
            final var repo = artifactRepo.getStorage().getRepository();
            try (RepositoryConnection con = repo.getConnection()) {
                root = constructVisualAreaTree(con, sourcePage, atree, areaTreeIri,
                        areaUris, atree.getAdditionalStatements());
            }
            if (root != null)
            {
                recursiveUpdateTopologies(root);
                atree.setRoot(root);
                atree.setAreaIris(areaUris);
                return atree;
            }
            else
                return null; // couldn't construct the area tree
        }
        else
            return null;
    }
    
    private RDFArea constructVisualAreaTree(RepositoryConnection con, RDFPage sourcePage, RDFAreaTree atree,
            IRI areaTreeIri, Map<IRI, RDFArea> areas,
            Collection<Statement> additionalStatements) throws RepositoryException
    {
        // find area IRIs
        final Set<Resource> areaIris = new HashSet<>();
        try (RepositoryResult<Statement> result = con.getStatements(null, RDF.TYPE, SEGM.Area, areaTreeIri)) {
            for (Statement st : result)
                areaIris.add(st.getSubject());
        }
        
        // create areas
        List<RDFArea> areaList = new ArrayList<>(areaIris.size());
        for (Resource res : areaIris)
        {
            if (res instanceof IRI)
            {
                final RDFArea area = createArea(con, sourcePage, areaTreeIri, (IRI) res, additionalStatements);
                area.setAreaTree(atree);
                areaList.add(area);
            }
        }

        // sort and put to map
        areaList.sort(new Comparator<RDFArea>() {
            @Override
            public int compare(RDFArea o1, RDFArea o2)
            {
                return o1.getDocumentOrder() - o2.getDocumentOrder();
            }
        });
        for (RDFArea area : areaList)
            areas.put(area.getIri(), area);
        
        //construct the tree
        Set<RDFArea> rootAreas = new HashSet<RDFArea>(areaList);
        try (RepositoryResult<Statement> result = con.getStatements(null, SEGM.isChildOf, null, areaTreeIri)) {
            for (Statement st : result)
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
        }
        if (rootAreas.size() == 1)
        {
            final RDFArea root = rootAreas.iterator().next();
            checkChildOrderValues(root);
            return root;
        }
        else
        {
            log.error("Strange number of root areas: {}", rootAreas.toString());
            return null; //strange number of root nodes
        }
    }
    
    private RDFArea createArea(RepositoryConnection con, RDFPage sourcePage,
            IRI areaTreeIri, IRI areaIri, Collection<Statement> additionalStatements) throws RepositoryException
    {
        RDFArea area = new RDFArea(new Rectangular(), areaIri);
        area.setId(next_id++);
        area.setDocumentOrder(-1);
        Map<IRI, Float> tagSupport = new HashMap<>(); //tagUri->support
        RDFTextStyle style = new RDFTextStyle();
        
        try (RepositoryResult<Statement> result = con.getStatements(areaIri, null, null, areaTreeIri)) {
            for (Statement st : result)
            {
                final IRI pred = st.getPredicate();
                final Value value = st.getObject();
                
                if (processContentRectProperty(con, pred, value, area) || processStyleProperty(pred, value, style))
                {
                    // sucessfully processed
                }
                else if (RDFS.LABEL.equals(pred))
                {
                    String name = value.stringValue();
                    area.setName(name);
                }
                else if (BOX.documentOrder.equals(pred))
                {
                    if (value instanceof Literal)
                        area.setDocumentOrder(((Literal) value).intValue());
                }
                else if (BOX.bounds.equals(pred))
                {
                    if (value instanceof IRI)
                    {
                        final Rectangular rect = createBounds(con, (IRI) value);
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
                            Tag tag = getTag((IRI) value);
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
                        try (RepositoryResult<Statement> tsResult = con.getStatements(tsUri, null, null)) {
                            for (Statement sst : tsResult)
                            {
                                if (SEGM.hasTag.equals(sst.getPredicate()) && sst.getObject() instanceof IRI)
                                    tagUri = (IRI) sst.getObject();
                                else if (SEGM.support.equals(sst.getPredicate()) && sst.getObject() instanceof Literal)
                                    support = ((Literal) sst.getObject()).floatValue();
                            }
                        }
                        if (tagUri != null && support != null)
                        {
                            Tag tag = getTag(tagUri);
                            if (tag != null)
                            {
                                area.removeTag(tag); //to remove the possible old 1.0f value
                                area.addTag(tag, support);
                                tagSupport.put(tagUri, support);
                            }
                        }
                    }
                }
                else
                {
                    // the statement was not used, keep it in additional statements
                    additionalStatements.add(st);
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
    
}
