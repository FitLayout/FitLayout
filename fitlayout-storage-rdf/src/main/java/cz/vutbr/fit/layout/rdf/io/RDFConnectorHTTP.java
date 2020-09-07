/**
 * RDFConnectorSesame.java
 *
 * Created on 9. 1. 2016, 13:17:59 by burgetr
 */
package cz.vutbr.fit.layout.rdf.io;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

/**
 * A RDF connector for a remote (HTTP) RDF4J server.
 * 
 * @author burgetr
 */
public class RDFConnectorHTTP extends RDFConnector
{
    private String serverUrl;
    private String repositoryId;


    public RDFConnectorHTTP(String serverUrl, String repositoryId) throws RepositoryException
    {
        this.serverUrl = serverUrl;
        this.repositoryId = repositoryId;
        initRepository();
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    @Override
    protected void initRepository() throws RepositoryException
    {
        if (!serverUrl.isEmpty() && !repositoryId.isEmpty())
        {
            repo = new HTTPRepository(serverUrl, repositoryId);
            repo.init();
            connection = repo.getConnection();
        }
        else
            throw new RepositoryException("Unknown endpoint URL format for Sesame; the expected format is <sevrer_url>/repositories/<repository_ID>");
    }

}
