/**
 * ConnectionSetArtifactService.java
 *
 * Created on 14. 6. 2022, 13:55:23 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;

/**
 * Base implementation that provides storing connections to a RDF repository.
 * 
 * @author burgetr
 */
public abstract class ConnectionSetArtifactService extends BaseArtifactService
{

    /**
     * Adds a subgraph representing a collection of connections to the RDF repository.
     * 
     * @param artifactIri the IRI of the artifact the connections belong to.
     * @param cons the connections to be stored.
     * @throws ServiceException
     */
    protected void saveConnections(IRI artifactIri, Collection<AreaConnection> cons) throws ServiceException
    {
        var repo = getServiceManager().getArtifactRepository();
        if (repo instanceof RDFArtifactRepository)
        {
            final var rdfRepo = (RDFArtifactRepository) repo;
            // create the subgraph with connections
            ConnectionSetModelBuilder builder = new ConnectionSetModelBuilder(rdfRepo.getIriFactory());
            final Model graph = builder.createModel(artifactIri, cons);
            // insert the processedBy statement
            graph.add(artifactIri, FL.processedBy, Values.literal(getId()));
            // insert the subgraph into the repository
            rdfRepo.getStorage().insertGraph(graph, artifactIri);
        }
        else
        {
            throw new ServiceException("RDFArtifactRepository is required for storing the connections");
        }
    }
    
}
