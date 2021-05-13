/**
 * ChunkSetLoader.java
 *
 * Created on 10. 4. 2021, 21:16:15 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFChunkSet;
import cz.vutbr.fit.layout.rdf.model.RDFPage;
import cz.vutbr.fit.layout.rdf.model.RDFTextChunk;

/**
 * 
 * @author burgetr
 */
public class ChunkSetModelLoader extends ModelLoaderBase implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(ChunkSetModelLoader.class);

    private static final String[] dataObjectProperties = new String[] { 
            "segm:hasTag",
            "segm:tagSupport"
    };
    
    public ChunkSetModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo) throws RepositoryException
    {
        return constructChunkSet(artifactRepo, artifactIri);
    }
    
    //================================================================================================
    
    private RDFChunkSet constructChunkSet(RDFArtifactRepository artifactRepo, IRI csetIri) throws RepositoryException
    {
        Model artifactModel = artifactRepo.getStorage().getSubjectModel(csetIri);
        if (artifactModel.size() > 0)
        {
            ChunkSetInfo csetInfo = new ChunkSetInfo(artifactModel, csetIri);
            IRI parentIri = getPredicateIriValue(artifactModel, csetIri, FL.hasParentArtifact);
            RDFChunkSet cset = new RDFChunkSet(parentIri);
            csetInfo.applyToChunkSet(cset);
            //load the model
            Model chunkModel = getChunkModelForSet(artifactRepo, csetIri);
            Model tagModel = getChunkTagModelForSet(artifactRepo, csetIri);
            //load the source area tree and page
            RDFAreaTree sourceAreaTree = null;
            RDFPage sourcePage = null;
            if (cset.getAreaTreeIri() != null)
            {
                sourceAreaTree = getSourceAreaTree(cset.getAreaTreeIri(), artifactRepo);
                if (sourceAreaTree != null)
                {
                    sourcePage = getSourcePage(sourceAreaTree.getPageIri(), artifactRepo);
                }
            }
            else
                log.error("ChunkSet {} has no area tree IRI", csetIri.toString());
            //construct the tree
            final Map<IRI, RDFTextChunk> chunkUris = new LinkedHashMap<>();
            final Set<TextChunk> chunks = loadChunks(artifactRepo, sourceAreaTree, sourcePage, csetIri, chunkModel, tagModel, chunkUris);
            cset.setTextChunks(chunks);
            return cset;
        }
        else
            return null;
    }

    private Set<TextChunk> loadChunks(RDFArtifactRepository artifactRepo, RDFAreaTree sourceAreaTree, RDFPage sourcePage,
                                        IRI csetIri, Model model, Model tagModel, Map<IRI, RDFTextChunk> chunkUris)
    {
        //find all chunks
        for (Resource res : model.subjects())
        {
            if (res instanceof IRI)
            {
                RDFTextChunk area = createChunkFromModel(artifactRepo, sourceAreaTree, sourcePage, model, tagModel, csetIri, (IRI) res);
                chunkUris.put((IRI) res, area);
            }
        }
        //create the resulting set
        final Set<TextChunk> ret = new HashSet<>();
        ret.addAll(chunkUris.values());
        return ret;
    }
    
    private RDFTextChunk createChunkFromModel(RDFArtifactRepository artifactRepo, RDFAreaTree sourceAreaTree, RDFPage sourcePage, 
                                                Model model, Model dataModel, IRI csetIri, IRI iri) throws RepositoryException
    {
        RDFTextChunk chunk = new RDFTextChunk(iri);
        Map<IRI, Float> tagSupport = new HashMap<>(); //tagUri->support
        
        for (Statement st : model.filter(iri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (SEGM.hasText.equals(pred)) 
            {
                chunk.setText(value.stringValue());
            }
            else if (BOX.backgroundColor.equals(pred))
            {
                final String bgColor = value.stringValue();
                chunk.setEffectiveBackgroundColor(Serialization.decodeHexColor(bgColor));
            }
            else if (SEGM.hasSourceArea.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Area a = sourceAreaTree.findAreaByIri((IRI) value);
                    if (a != null)
                        chunk.setSourceArea(a);
                    else
                        log.error("hasSourceArea points to a non-existent area {}", value.toString());
                }                
            }
            else if (SEGM.hasSourceBox.equals(pred))
            {
                if (value instanceof IRI)
                {
                    final Box b = sourcePage.findBoxByIri((IRI) value);
                    if (b != null)
                        chunk.setSourceBox(b);
                    else
                        log.error("hasSourceBox points to a non-existent box {}", value.toString());
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
                            chunk.addTag(tag, 1.0f); //spport is unkwnown (yet)
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
                            chunk.addTag(tag, support);
                    }
                }
            }
            
        }
        
        return chunk;
    }

    
    //================================================================================================
    
    /**
     * Obtains the model of chunks for the given chunk set.
     * @param artifactRepo the repository to query 
     * @param chunkSetIri the area tree IRI
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    private Model getChunkModelForSet(RDFArtifactRepository artifactRepo, IRI chunkSetIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:TextChunk . "
                + "?s segm:belongsToChunkSet <" + chunkSetIri.stringValue() + "> }";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }
    
    private Model getChunkTagModelForSet(RDFArtifactRepository artifactRepo, IRI chunkSetIri) throws RepositoryException
    {
        final String query = artifactRepo.getIriDecoder().declarePrefixes()
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:TextChunk . "
                + "?a segm:belongsToChunkSet <" + chunkSetIri.stringValue() + "> . "
                + getDataPropertyUnion(dataObjectProperties)
                + "}";
        return artifactRepo.getStorage().executeSafeQuery(query);
    }

}
