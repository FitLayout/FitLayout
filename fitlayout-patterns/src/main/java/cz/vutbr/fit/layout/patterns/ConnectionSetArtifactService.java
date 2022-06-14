/**
 * ConnectionSetArtifactService.java
 *
 * Created on 14. 6. 2022, 13:55:23 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;

/**
 * 
 * @author burgetr
 */
public abstract class ConnectionSetArtifactService extends BaseArtifactService
{

    protected void saveConnections(IRI artifactIri, Collection<AreaConnection> cons) throws ServiceException
    {
        var repo = getServiceManager().getArtifactRepository();
        if (repo instanceof RDFArtifactRepository)
        {
            final var rdfRepo = (RDFArtifactRepository) repo;
            ConnectionSetModelBuilder builder = new ConnectionSetModelBuilder(rdfRepo.getIriFactory());
            final Model graph = builder.createModel(artifactIri, cons);
            rdfRepo.getStorage().insertGraph(graph, artifactIri);
        }
        else
        {
            throw new ServiceException("RDFArtifactRepository is required for storing the connections");
        }
    }
    
}
