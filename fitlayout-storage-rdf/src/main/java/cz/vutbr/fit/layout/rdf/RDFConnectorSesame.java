/**
 * RDFConnectorSesame.java
 *
 * Created on 9. 1. 2016, 13:17:59 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

/**
 * A RDF connector optimized for the Sesame remote server.
 * @author burgetr
 */
public class RDFConnectorSesame extends RDFConnector
{

    public RDFConnectorSesame(String endpoint) throws RepositoryException
    {
        super(endpoint);
    }

    @Override
    protected void initRepository() throws RepositoryException
    {
        //analyse the endpoint url in order to obtain the server url and the repository name
        //the expected format is <sevrer_url>/repositories/<repository_ID>
        final String splitter = "/repositories";
        String serverUrl = "";
        String repositoryId = "";
        String url = endpointUrl;
        while (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        int pos = url.lastIndexOf('/');
        if (pos != -1)
        {
            repositoryId = url.substring(pos + 1);
            url = url.substring(0, pos);
        }
        if (url.endsWith(splitter))
        {
            serverUrl = url.substring(0, url.length() - splitter.length());
        }
            
        if (!serverUrl.isEmpty() && !repositoryId.isEmpty()) //valid URL found
        {
            repo = new HTTPRepository(serverUrl, repositoryId);
            repo.initialize();
            connection = repo.getConnection();
        }
        else
            throw new RepositoryException("Unknown endpoint URL format for Sesame; the expected format is <sevrer_url>/repositories/<repository_ID>");
    }

}
