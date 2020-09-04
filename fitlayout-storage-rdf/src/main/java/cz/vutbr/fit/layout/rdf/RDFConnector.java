package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * 
 * 
 * @author milicka
 * @author burgetr
 */
public class RDFConnector
{
	protected String endpointUrl;
	protected RepositoryConnection connection;
	protected Repository repo;
	protected ValueFactory vf;

	/**
	 * Establishes a connection to the SPARQL endpoint.
	 * @param endpoint the SPARQL endpoint URL
	 * @throws RepositoryException
	 */
	public RDFConnector(String endpoint) throws RepositoryException 
	{
		endpointUrl = endpoint;
		connection = null;
		vf = SimpleValueFactory.getInstance();
		initRepository();
	}
	
    /**
     * Obtains current connection to the repository or opens a new one when no connection
     * has been opened.
     * @return the connection object
     * @throws RepositoryException 
     */
    public RepositoryConnection getConnection() throws RepositoryException 
    {
        if (connection == null)
            connection = repo.getConnection();
        return connection;
    }

    /**
     * Closes the current connection.
     * @throws RepositoryException
     */
    public void closeConnection() throws RepositoryException
    {
        if (connection != null)
            connection.close();
        connection = null;
    }
    
    /**
     * Creates a new connection.
     * @throws RepositoryException
     * @throws RepositoryConfigException 
     */
    protected void initRepository() throws RepositoryException
    {
        repo = new SPARQLRepository(endpointUrl);
        repo.initialize();
    }
    
	/**
	 * Adds single tripple to the repository.
	 * @param s
	 * @param p
	 * @param o
	 * @throws RepositoryException
	 */
	public void add(Resource s, IRI p, Value o) 
	{
		try {
			
			Statement stmt = vf.createStatement(s, p, o);
			this.connection.add(stmt);
			this.connection.commit();
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a SPARQL query and returns the result.
	 * @param queryString
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 */
	public TupleQueryResult executeQuery(String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException 
	{
		try {
			TupleQuery query = this.connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult tqr = query.evaluate();
        	return tqr;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * in BigData 1.4 it is unimplemented function
	 * @param newNamespace
	 */
	public void addNamespace(String newNamespace) 
	{
		
	/*	
		final Properties props = new Properties();
		props.put(BigdataSail.Options.NAMESPACE, newNamespace);
*/
		
		/*
		final RemoteRepositoryManager repo = new RemoteRepositoryManager("http://localhost:8080/bigdata/sparql");
		
		try {
			repo.initialize();
			repo.getAllRepositories();
		} catch (RepositoryConfigException | RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/
		
		/*
		ClientConnectionManager m_cm = DefaultClientConnectionManagerFactory.getInstance().newInstance();;
		final DefaultHttpClient httpClient = new DefaultHttpClient(m_cm);

		httpClient.setRedirectStrategy(new DefaultRedirectStrategy());
		
		final ExecutorService executor = Executors.newCachedThreadPool();

			//final RemoteRepositoryManager m_repo = new RemoteRepositoryManager(endpointUrl, httpClient, executor);
		com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager rrm = new com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager(endpointUrl, httpClient, executor);
		RemoteRepository rr = rrm.getRepositoryForURL(endpointUrl);
		try {
			rrm.getRepositoryDescriptions();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}
	
	public void removeNamespace(String namespace) throws RepositoryException 
	{
		repo.getConnection().removeNamespace(namespace);
	}
	
	public RepositoryResult<Namespace> getAllNamespaces() throws RepositoryException 
	{
		return repo.getConnection().getNamespaces();
	}
}
