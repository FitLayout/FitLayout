/**
 * RDFArtifactRepository.java
 *
 * Created on 30. 9. 2020, 15:00:47 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Implementation of an ArtifactRepository on top of an RDFStorage.
 * 
 * @author burgetr
 */
public class RDFArtifactRepository implements ArtifactRepository
{
    private static final String METADATA_CONTEXT_PREFIX = "file://resources/rdf/";
    private static final String SAVED_QUERIES_CONTEXT = "http://fitlayout.github.io/queries/";

    private static Logger log = LoggerFactory.getLogger(RDFArtifactRepository.class);
    
    public static String METADATA_SUFFIX = "meta";
    
    /** Required OWL resources containing the storage metadata */
    private static String[] owls = new String[] {"render.owl", "segmentation.owl", "fitlayout.owl", "mapping.owl"};
    
    private boolean readOnly;
    private RDFStorage storage;
    private IRIFactory iriFactory;
    private RDFIRIDecoder iriDecoder;
    private Map<IRI, ModelBuilder> modelBuilders;
    private Map<IRI, ModelLoader> modelLoaders;

    
    public RDFArtifactRepository(RDFStorage storage)
    {
        readOnly = false;
        this.storage = storage;
        iriFactory = new DefaultIRIFactory();
        iriDecoder = new RDFIRIDecoder();
        initDefaultModelBuilders();
        init();
    }

    public static RDFArtifactRepository createMemory(String path)
    {
        return new RDFArtifactRepository(RDFStorage.createMemory(path));
    }
    
    public static RDFArtifactRepository createNative(String path)
    {
        return new RDFArtifactRepository(RDFStorage.createNative(path));
    }
    
    public static RDFArtifactRepository createHTTP(String serverUrl, String repositoryId)
    {
        return new RDFArtifactRepository(RDFStorage.createHTTP(serverUrl, repositoryId));
    }
    
    @Override
    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    @Override
    public void disconnect()
    {
        storage.close();
    }

    public RDFStorage getStorage()
    {
        return storage;
    }

    @Override
    public IRIDecoder getIriDecoder()
    {
        return iriDecoder;
    }

    /**
     * Gets the IRI factory used for creating the IRIs when building a RDF graph.
     */
    public IRIFactory getIriFactory()
    {
        return iriFactory;
    }

    /**
     * Configures the IRI factory used for creating the IRIs when building a RDF graph.
     * @param iriFactory
     */
    public void setIriFactory(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }
    
    //Init and health check ==========================================================
    
    /**
     * Checks the repository status and initializes the metadata when necessary
     */
    public void init()
    {
        if (!isInitialized())
        {
            if (System.getProperty("fitlayout.rdf.disableAutoInit") == null)
            {
                initMetadata();
                if (!isInitialized())
                    log.error("Repository init failed");
            }
        }
    }
    
    /**
     * Checks whether the storage has been initialized - it seems to contain the appropriate
     * metadata
     * @return {@code true} when the repository is ready to use
     */
    public boolean isInitialized()
    {
        final Value val = getStorage().getPropertyValue(BOX.Page, RDF.TYPE);
        return (val != null);
    }
    
    /**
     * Initializes the repository metadata using the default OWL resource files.
     * @return {@code true} when the repository was initialized sucessfully.
     */
    public boolean initMetadata()
    {
        log.info("Initializing repository metadata");
        try {
            for (String owl : owls)
            {
                final ValueFactory vf = SimpleValueFactory.getInstance();
                final IRI context = vf.createIRI(METADATA_CONTEXT_PREFIX + owl);
                String owlFile = loadResource("/rdf/" + owl);
                getStorage().importXML(owlFile, context);
            }
        } catch (Exception e) {
            log.error("Could import metadata: {}", e);
            return false;
        }
        return isInitialized();
    }
    
    private static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(RDFArtifactRepository.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }
    
    public void clearMetadata()
    {
        log.info("Clearing repository metadata");
        try {
            for (String owl : owls)
            {
                final ValueFactory vf = SimpleValueFactory.getInstance();
                final IRI context = vf.createIRI(METADATA_CONTEXT_PREFIX + owl);
                getStorage().clear(context);
            }        
        } catch (Exception e) {
            log.error("Could clear metadata: {}", e);
        }
    }
    
    //Artifact functions =============================================================
    
    @Override
    public Collection<IRI> getArtifactIRIs() throws StorageException
    {
        try {
            final String query = iriDecoder.declarePrefixes()
                    + "SELECT DISTINCT ?pg "
                    + "WHERE {"
                    + "  ?pg rdf:type ?type . "
                    + "  ?type rdfs:subClassOf fl:Artifact . "
                    + "  OPTIONAL { ?pg fl:createdOn ?ctime } "
                    + "  bind(coalesce(?ctime, 0) as ?time) "
                    + "} ORDER BY ?time";
            
            List<BindingSet> data = storage.executeSafeTupleQuery(query);
            List<IRI> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                Binding b = binding.getBinding("pg");
                ret.add((IRI) b.getValue());
            }
            return ret;
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Gets the artifact IRIs for a source page only
     * @param pageIri the source page IRI
     * @return the list of artifact IRIs
     * @throws StorageException
     */
    public Collection<IRI> getArtifactIRIs(IRI pageIri) throws StorageException
    {
        try {
            final String query = iriDecoder.declarePrefixes()
                    + "SELECT DISTINCT ?pg "
                    + "WHERE {"
                    + "  ?pg segm:hasSourcePage <" + pageIri.toString() + "> . "
                    + "  ?pg rdf:type ?type . "
                    + "  ?type rdfs:subClassOf fl:Artifact . "
                    + "  OPTIONAL { ?pg fl:createdOn ?ctime } "
                    + "  bind(coalesce(?ctime, 0) as ?time) "
                    + "} ORDER BY ?time";
            
            List<BindingSet> data = storage.executeSafeTupleQuery(query);
            List<IRI> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                Binding b = binding.getBinding("pg");
                ret.add((IRI) b.getValue());
            }
            return ret;
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    @Override
    public Collection<Artifact> getArtifactInfo() throws StorageException
    {
        try {
            final String query = iriDecoder.declarePrefixes()
                    + "SELECT DISTINCT ?pg ?type ?label ?time ?parent ?creator ?creatorParams\n"
                    + "WHERE {\n"
                    + "  ?pg rdf:type ?type .\n"
                    + "  ?type rdfs:subClassOf fl:Artifact .\n"
                    + "  ?pg rdfs:label ?label .\n"
                    + "  ?pg fl:creator ?creator .\n"
                    + "  ?pg fl:creatorParams ?creatorParams .\n"
                    + "  OPTIONAL { ?pg fl:hasParentArtifact ?parent } .\n"
                    + "  OPTIONAL { ?pg fl:createdOn ?time }\n"
                    + "} ORDER BY ?time\n";
            
            List<BindingSet> data = storage.executeSafeTupleQuery(query);
            List<Artifact> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                ret.add(new RDFArtifactInfo(binding));
            }
            return ret;
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    public Collection<Artifact> getArtifactInfoOld()
    {
        Collection<IRI> iris = getArtifactIRIs();
        List<Artifact> ret = new ArrayList<>(iris.size());
        for (IRI iri : iris)
        {
            Model artifactModel = getStorage().getSubjectModel(iri);
            if (artifactModel.size() > 0)
            {
                RDFArtifactInfo info = new RDFArtifactInfo(artifactModel, iri, false);
                ret.add(info);
            }
        }
        return ret;
    }

    @Override
    public Artifact getArtifact(IRI artifactIri)
    {
        IRI type = getArtifactType(artifactIri);
        if (type != null)
        {
            ModelLoader loader = getModelLoader(type);
            if (loader != null)
            {
                Artifact artifact = loader.loadArtifact(artifactIri, this);
                return artifact;
            }
            else
                log.warn("No loader available for type {}", type);
        }
        else
            log.warn("Artifact {} has no type", artifactIri);
        return null;
    }
    
    public Model getArtifactModel(IRI artifactIri)
    {
        //we use a separate context for each artifact
        return storage.getContextModel(artifactIri);
    }

    @Override
    public void addArtifact(Artifact artifact)
    {
        if (isReadOnly())
            throw new StorageException("Read-only repository");
            
        if (artifact.getIri() == null)
            artifact.setIri(createArtifactIri(artifact));
        
        log.debug("STORING {}", artifact);
        ModelBuilder builder = getModelBuilder(artifact.getArtifactType());
        if (builder != null)
        {
            Model graph = builder.createGraph(artifact);
            try {
                storage.insertGraph(graph, artifact.getIri());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (artifact.getMetadata() != null)
            {
                final IRI metaIRI = getMetadataIRI(artifact.getIri());
                Model metadata = MetadataExtractor.extract(artifact);
                storage.insertGraph(metadata, metaIRI);
            }
        }
        else
            log.error("Could not find RDF model builder for artifact {}, type {}", artifact, artifact.getArtifactType());
    }

    @Override
    public IRI createArtifactIri(Artifact artifact)
    {
        long seq = storage.getNextSequenceValue(iriFactory.createSequenceURI("page"));
        IRI pageUri = iriFactory.createArtifactIri(seq);
        return pageUri;
    }
    
    @Override
    public void replaceArtifact(IRI artifactIri, Artifact artifact)
    {
        if (isReadOnly())
            throw new StorageException("Read-only repository");
        artifact.setIri(artifactIri);
        clearArtifact(artifactIri);
        addArtifact(artifact);
    }

    @Override
    public void removeArtifact(IRI artifactIri)
    {
        if (isReadOnly())
            throw new StorageException("Read-only repository");
        //clear the derived artifacts
        List<Artifact> derived = new ArrayList<>();
        findDerivedArtifacts(artifactIri, getArtifactInfo(), derived);
        for (Artifact a : derived)
            clearArtifact(a.getIri());
        //clear the artifact itself
        clearArtifact(artifactIri);
    }
    
    public IRI getMetadataIRI(IRI artifactIri)
    {
        return iriFactory.createRelatedIri(artifactIri, METADATA_SUFFIX);
    }
    
    /**
     * Removes the artifact subpgraph and the related subgraphs
     * without checking the derived artifacts.
     * @param artifactIri the artifact IRI to remove
     */
    private void clearArtifact(IRI artifactIri)
    {
        storage.clear(artifactIri);
        storage.clear(getMetadataIRI(artifactIri));
    }
    
    private void findDerivedArtifacts(IRI artifactIri, Collection<Artifact> artifacts, List<Artifact> dest)
    {
        for (Artifact a : artifacts)
        {
            if (artifactIri.equals(a.getParentIri()))
            {
                findDerivedArtifacts(a.getIri(), artifacts, dest);
                dest.add(a);
            }
        }
    }

    @Override
    public void clear()
    {
        if (isReadOnly())
            throw new StorageException("Read-only repository");
        storage.clear();
    }
    
    public void clearContext(IRI contextIri)
    {
        if (isReadOnly())
            throw new StorageException("Read-only repository");
        storage.clear(contextIri);
    }

    /**
     * Determines a stored artifact type.
     * @param artifactIri the artifact IRI
     * @return the type IRI or {@code null} when no type declaration (rdf:type) was found.
     */
    private IRI getArtifactType(IRI artifactIRI)
    {
        final Value val = storage.getPropertyValue(artifactIRI, RDF.TYPE);
        if (val.isIRI())
            return (IRI) val;
        else
            return null;
    }
    
    //Model builders =================================================================

    protected void initDefaultModelBuilders()
    {
        modelBuilders = new HashMap<>();
        modelLoaders = new HashMap<>();
        addModelBuilder(BOX.Page, new BoxModelBuilder(iriFactory));
        addModelBuilder(SEGM.AreaTree, new AreaModelBuilder(iriFactory));
        addModelBuilder(SEGM.LogicalAreaTree, new LogicalAreaModelBuilder(iriFactory));
        addModelBuilder(SEGM.ChunkSet, new ChunkSetModelBuilder(iriFactory));
        addModelLoader(BOX.Page, new BoxModelLoader(iriFactory));
        addModelLoader(SEGM.AreaTree, new AreaModelLoader(iriFactory));
        addModelLoader(SEGM.LogicalAreaTree, new LogicalAreaModelLoader(iriFactory));
        addModelLoader(SEGM.ChunkSet, new ChunkSetModelLoader(iriFactory));
    }
    
    public void addModelBuilder(IRI artifactType, ModelBuilder builder)
    {
        modelBuilders.put(artifactType, builder);
    }
    
    public ModelBuilder getModelBuilder(IRI artifactType)
    {
        return modelBuilders.get(artifactType);
    }
    
    public void addModelLoader(IRI artifactType, ModelLoader builder)
    {
        modelLoaders.put(artifactType, builder);
    }
    
    public ModelLoader getModelLoader(IRI artifactType)
    {
        return modelLoaders.get(artifactType);
    }

    //Tags =============================================================
    
    //@Override
    public Collection<Tag> getTags() throws StorageException
    {
        try {
            final String query = iriDecoder.declarePrefixes()
                    + "SELECT ?tag ?name ?type WHERE { "
                    + "    ?tag segm:name ?name . "
                    + "    ?tag segm:type ?type . "
                    + "    ?tag rdf:type segm:Tag "
                    + "}";
            
            List<BindingSet> data = storage.executeSafeTupleQuery(query);
            List<Tag> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                Binding bIri = binding.getBinding("tag");
                Binding bName = binding.getBinding("name");
                Binding bType = binding.getBinding("type");
                if (bIri != null && bName != null && bType != null && bIri.getValue() instanceof IRI)
                {
                    Tag tag = new DefaultTag((IRI) bIri.getValue(), bType.getValue().stringValue(), bName.getValue().stringValue());
                    ret.add(tag);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    //= Saved queries =========================================================================
    
    public void saveQuery(SavedQuery query)
    {
        if (query.getIri() == null)
        {
            long seq = storage.getNextSequenceValue(iriFactory.createSequenceURI("query"));
            IRI iri = iriFactory.createSavedQueryURI(seq);
            query.setIri(iri);
        }
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI context = vf.createIRI(SAVED_QUERIES_CONTEXT);
        storage.add(query.getIri(), RDF.TYPE, FL.SavedQuery, context);
        storage.addValue(query.getIri(), RDFS.LABEL, query.getTitle(), context);
        storage.addValue(query.getIri(), RDF.VALUE, query.getQueryString(), context);
    }
    
    public Map<IRI, SavedQuery> getSavedQueries()
    {
        try {
            final String query = iriDecoder.declarePrefixes()
                    + "SELECT ?query ?label ?queryString WHERE { "
                    + "    ?query rdfs:label ?label . "
                    + "    ?query rdf:value ?queryString . "
                    + "    ?query rdf:type fl:SavedQuery "
                    + "}";
            
            Map<IRI, SavedQuery> ret = new HashMap<>();
            List<BindingSet> data = storage.executeSafeTupleQuery(query);
            for (BindingSet binding : data)
            {
                Binding bIri = binding.getBinding("query");
                Binding bLabel = binding.getBinding("label");
                Binding bQuery = binding.getBinding("queryString");
                if (bIri != null && bLabel != null && bQuery != null && bIri.getValue() instanceof IRI)
                {
                    SavedQuery newq = new SavedQuery((IRI) bIri.getValue(), bLabel.getValue().stringValue(), bQuery.getValue().stringValue());
                    ret.put(newq.getIri(), newq);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void deleteSavedQuery(IRI iri)
    {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI context = vf.createIRI(SAVED_QUERIES_CONTEXT);
        storage.removeStatements(iri, null, null, context);
    }

}
