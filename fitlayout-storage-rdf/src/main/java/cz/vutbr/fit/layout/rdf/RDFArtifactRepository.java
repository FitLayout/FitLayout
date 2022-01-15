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

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFTag;

/**
 * Implementation of an ArtifactRepository on top of an RDFStorage.
 * 
 * @author burgetr
 */
public class RDFArtifactRepository implements ArtifactRepository
{
    private static Logger log = LoggerFactory.getLogger(RDFArtifactRepository.class);
    
    /** Required OWL resources containing the storage metadata */
    private static String[] owls = new String[] {"render.owl", "segmentation.owl", "fitlayout.owl", "mapping.owl"};
    
    private RDFStorage storage;
    private IRIFactory iriFactory;
    private RDFIRIDecoder iriDecoder;
    private Map<IRI, ModelBuilder> modelBuilders;
    private Map<IRI, ModelLoader> modelLoaders;

    
    public RDFArtifactRepository(RDFStorage storage)
    {
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
                final IRI context = vf.createIRI("file://resources/rdf/" + owl);
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
                final IRI context = vf.createIRI("file://resources/rdf/" + owl);
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
                    + "  OPTIONAL { ?pg fl:createdOn ?time } "
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
    public Collection<Artifact> getArtifactInfo()
    {
        Collection<IRI> iris = getArtifactIRIs();
        List<Artifact> ret = new ArrayList<>(iris.size());
        for (IRI iri : iris)
        {
            Model artifactModel = getStorage().getSubjectModel(iri);
            if (artifactModel.size() > 0)
            {
                RDFArtifactInfo info = new RDFArtifactInfo(artifactModel, iri);
                ret.add(info);
            }
        }
        return ret;
    }

    @Override
    public Artifact getArtifact(IRI artifactIri)
    {
        Model model = getArtifactModel(artifactIri);
        IRI type = getArtifactType(model, artifactIri);
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
        if (artifact.getIri() == null)
            artifact.setIri(createArtifactIri(artifact));
        
        log.debug("STORING {}", artifact);
        ModelBuilder builder = getModelBuilder(artifact.getArtifactType());
        if (builder != null)
        {
            Model graph = builder.createGraph(artifact);
            storage.insertGraph(graph, artifact.getIri());
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
        artifact.setIri(artifactIri);
        storage.clear(artifactIri);
        addArtifact(artifact);
    }

    @Override
    public void removeArtifact(IRI artifactIri)
    {
        //clear the derived artifacts
        List<Artifact> derived = new ArrayList<>();
        findDerivedArtifacts(artifactIri, getArtifactInfo(), derived);
        for (Artifact a : derived)
            storage.clear(a.getIri());
        //clear the artifact itself
        storage.clear(artifactIri);
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
        storage.clear();
    }
    
    public void clearContext(IRI contextIri)
    {
        storage.clear(contextIri);
    }

    /**
     * Gets the artifact type from an artifact model.
     * @param model the artifact model
     * @param artifactIri the artifact IRI
     * @return the type IRI or {@code null} when no type declaration (rdf:type) was found.
     */
    private IRI getArtifactType(Model model, IRI artifactIri)
    {
        Iterable<Statement> typeStatements = model.getStatements(artifactIri, RDF.TYPE, null);
        for (Statement st : typeStatements)
        {
            if (st.getObject() instanceof IRI)
                return (IRI) st.getObject();
        }
        return null; //no type statement found
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
        addModelBuilder(BOX.ConnectionSet, new ConnectionSetModelBuilder(iriFactory));
        addModelLoader(BOX.Page, new BoxModelLoader(iriFactory));
        addModelLoader(SEGM.AreaTree, new AreaModelLoader(iriFactory));
        addModelLoader(SEGM.LogicalAreaTree, new LogicalAreaModelLoader(iriFactory));
        addModelLoader(SEGM.ChunkSet, new ChunkSetModelLoader(iriFactory));
        addModelLoader(BOX.ConnectionSet, new ConnectionSetModelLoader(iriFactory));
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
                    + "    ?tag rdf:type box:Tag "
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
                    RDFTag tag = new RDFTag((IRI) bIri.getValue(), bType.getValue().stringValue(), bName.getValue().stringValue());
                    ret.add(tag);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
}
