/**
 * RDFArtifactRepository.java
 *
 * Created on 30. 9. 2020, 15:00:47 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Implementation of an ArtifactRepository on top of a RDFStorage.
 * 
 * @author burgetr
 */
public class RDFArtifactRepository implements ArtifactRepository
{
    private static Logger log = LoggerFactory.getLogger(RDFArtifactRepository.class);
    
    private RDFStorage storage;
    private Map<IRI, ModelBuilder> modelBuilders;

    
    public RDFArtifactRepository(RDFStorage storage)
    {
        this.storage = storage;
        modelBuilders = new HashMap<>();
        initDefaultModelBuilders();
    }

    public RDFStorage getStorage()
    {
        return storage;
    }

    //Artifact functions =============================================================
    
    @Override
    public Collection<IRI> getArtifactIRIs() throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "SELECT ?pg "
                + "WHERE {"
                + "  ?pg rdf:type ?type . "
                + "  ?type rdfs:subClassOf layout:Artifact "
                + "}";
        
        log.debug("QUERY: {}", query);
        TupleQueryResult data = storage.executeSafeTupleQuery(query);
        Set<IRI> ret = new HashSet<IRI>();
        try
        {
            while (data.hasNext())
            {
                BindingSet binding = data.next();
                Binding b = binding.getBinding("pg");
                ret.add((IRI) b.getValue());
            }
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    @Override
    public Artifact getArtifact(IRI artifactIri)
    {
        // TODO Auto-generated method stub
        return null;
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
        long seq = storage.getNextSequenceValue("page");
        IRI pageUri = RESOURCE.createArtifactIri(seq);
        return pageUri;
    }
    
    //Model builders =================================================================

    protected void initDefaultModelBuilders()
    {
        addModelBuilder(BOX.Page, new BoxModelBuilder());
        addModelBuilder(SEGM.AreaTree, new AreaModelBuilder());
        addModelBuilder(SEGM.LogicalAreaTree, new LogicalAreaModelBuilder());
    }
    
    public void addModelBuilder(IRI artifactType, ModelBuilder builder)
    {
        modelBuilders.put(artifactType, builder);
    }
    
    public ModelBuilder getModelBuilder(IRI artifactType)
    {
        return modelBuilders.get(artifactType);
    }
    
    

}
