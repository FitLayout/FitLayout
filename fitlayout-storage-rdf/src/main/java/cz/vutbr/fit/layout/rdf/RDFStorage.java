package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.io.RDFConnector;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorHTTP;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorMemory;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorNative;


/**
 * This class provides an abstraction of an RDF repository and implements the common
 * low-level operations.
 * 
 * @author burgetr
 */
public class RDFStorage implements Closeable
{
    private static Logger log = LoggerFactory.getLogger(RDFStorage.class);
    
	private RDFConnector db;
    private Map<String, String> prefixUris; // prefix -> URI
    private Map<String, String> uriPrefixes; // URI -> prefix

	/**
	 * Use the create functions for creating the instances.
	 * @throws RepositoryException
	 */
	protected RDFStorage(RDFConnector connector) throws RepositoryException
	{
	    db = connector;
	}
	
	public static RDFStorage createMemory(String dataDir)
	{
        log.info("Using memory storage in {}", dataDir);
	    RDFStorage storage = new RDFStorage(new RDFConnectorMemory(dataDir));
	    return storage;
	}
	
    public static RDFStorage createNative(String dataDir)
    {
        log.info("Using native storage in {}", dataDir);
        RDFStorage storage = new RDFStorage(new RDFConnectorNative(dataDir));
        return storage;
    }
    
    public static RDFStorage createHTTP(String serverUrl, String repositoryId)
    {
        log.info("Using HTTP storage in {} : {}", serverUrl, repositoryId);
        RDFStorage storage = new RDFStorage(new RDFConnectorHTTP(serverUrl, repositoryId));
        return storage;
    }
    
    @Override
    public void close()
    {
        db.close();
    }

	/**
	 * Obtains a connection to the current repository.
	 * @return the repository connection.
	 * @throws RepositoryException 
	 */
	public RepositoryConnection getConnection() throws RepositoryException 
	{
		return db.getConnection();
	}
	
	public void closeConnection() throws RepositoryException
	{
	    db.closeConnection();
	}

	
    //= Low-level repository functions ==============================================================================
	
	/**
	 * Obtains all statements for the specific subject.
	 * (gets all triples for specific node)
	 * 
	 * @param subject
	 * @return
	 * @throws RepositoryException
	 */
	public RepositoryResult<Statement> getSubjectStatements(Resource subject) throws RepositoryException 
	{
		return getConnection().getStatements(subject, null, null, true);
	}
	
	/**
	 * Obtains the value of the given predicate for the given subject.
	 * @param subject the subject resource
	 * @param predicate the predicate IRI
	 * @return the resulting Value or {@code null} when there is no corresponding triplet available.
	 * @throws RepositoryException
	 */
	public Value getPropertyValue(Resource subject, IRI predicate) throws RepositoryException
	{
	    RepositoryResult<Statement> result = getConnection().getStatements(subject, predicate, null, true);
	    if (result.hasNext())
	        return result.next().getObject();
	    else
	        return null;
	}
	
	/**
	 * Obtains a model for the specific subject.
	 * @param subject
	 * @return
	 * @throws RepositoryException 
	 */
	public Model getSubjectModel(Resource subject) throws RepositoryException 
	{
	    RepositoryResult<Statement> result = getSubjectStatements(subject); 
		Model ret = createModel(result);
		result.close();
		closeConnection();
		return ret;
	}

	/**
	 * Executes a SPARQL query on the databse
	 * @param query the SPARQL query
	 * @return
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public TupleQueryResult executeQuery(String query) throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		org.eclipse.rdf4j.query.TupleQuery tq = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
		return tq.evaluate();
	}
	
	
	public void clearRDFDatabase() 
	{
		try {
			Update upd = getConnection().prepareUpdate(QueryLanguage.SPARQL, "DELETE WHERE { ?s ?p ?o }");
			upd.execute();
			closeConnection();
		} catch (MalformedQueryException | RepositoryException | UpdateExecutionException e) {
			e.printStackTrace();
		}
	}

	public void execSparqlUpdate(String query) throws RepositoryException, MalformedQueryException, UpdateExecutionException 
	{
        Update upd = getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
        upd.execute();
        closeConnection();
	}
	
    public void importTurtle(String query) throws RDFParseException, RepositoryException, IOException 
    {
        getConnection().add(new StringReader(query), "http://fitlayout.github.io/ontology/render.owl#", RDFFormat.TURTLE);
        closeConnection();
    }

    public void importXML(String query) throws RDFParseException, RepositoryException, IOException 
    {
        getConnection().add(new StringReader(query), "http://fitlayout.github.io/ontology/render.owl#", RDFFormat.RDFXML);
        closeConnection();
    }
	
	/**
	 * Executes a SPARQL query where the query syntax is safe (should not fail)
	 * @param query
	 * @return
	 * @throws RepositoryException
	 */
	private Model executeSafeQuery(String query) throws RepositoryException
	{
        try
        {
            GraphQuery pgq = getConnection().prepareGraphQuery(QueryLanguage.SPARQL, query);
            GraphQueryResult gqr = pgq.evaluate();
            Model ret = createModel(gqr);
            gqr.close();
            closeConnection();
            return ret;
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return new LinkedHashModel(); //this should not happen
	}
	
    /**
     * Executes an internal (safe) tuple query
     * @param query
     * @return a TupleQueryResult object representing the result
     * @throws RepositoryException
     */
	public TupleQueryResult executeSafeTupleQuery(String query) throws RepositoryException
    {
        try
        {
            TupleQuery pgq = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult gqr = pgq.evaluate();
            return gqr;
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null; //this should not happen
    }
    
	/**
	 * Creates a Model from the RepositoryResult
	 * @param result
	 * @return
	 * @throws RepositoryException 
	 */
	private Model createModel(RepositoryResult<Statement> result) throws RepositoryException 
	{
		Model model = new LinkedHashModel();
		while (result.hasNext())
			model.add(result.next());
		return model;
	}

    /**
     * Creates a model from a GraphQueryResult
     * @param result
     * @return
     * @throws QueryEvaluationException 
     */
    private Model createModel(GraphQueryResult result) throws QueryEvaluationException 
    {
        Model model = new LinkedHashModel();
        while (result.hasNext())
            model.add(result.next());
        return model;
    }

    /**
     * Create a set of subjects in a repository result.
     * @param result
     * @return
     * @throws RepositoryException
     */
    public Set<IRI> getSubjectsFromResult(RepositoryResult<Statement> result) throws RepositoryException 
    {
        Set<IRI> output = new HashSet<IRI>();
        while (result.hasNext()) 
        {
            Resource uri = result.next().getSubject();
            if (uri instanceof IRI)
                output.add((IRI) uri);
        }
        return output;
    }
    
	/**
	 * Inserts a new graph to the database.
	 * @param graph
	 * @throws RepositoryException 
	 */
    public void insertGraph(Model graph) throws RepositoryException
	{
        getConnection().begin();
		getConnection().add(graph);
		getConnection().commit();
		closeConnection();
	}

    /**
     * Inserts a new graph to the database.
     * @param graph
     * @param contextIri the context to be used for the inserted statements
     * @throws RepositoryException 
     */
    public void insertGraph(Model graph, IRI contextIri) throws RepositoryException
    {
        getConnection().begin();
        getConnection().add(graph, contextIri);
        getConnection().commit();
        closeConnection();
    }
    
    //= Sequences ==================================================================
    
    /**
     * Obtains the last assigned value of a sequence with the given name.
     * @param name the sequence name
     * @return the last assigned value or 0 when the sequence does not exist.
     * @throws RepositoryException 
     */
    public long getLastSequenceValue(String name) throws RepositoryException
    {
        IRI sequence = RESOURCE.createSequenceURI(name);
        RepositoryResult<Statement> result = getConnection().getStatements(sequence, RDF.VALUE, null, false);
        if (result.hasNext())
        {
            Value val = result.next().getObject();
            result.close();
            closeConnection();
            if (val instanceof Literal)
                return ((Literal) val).longValue();
            else
                return 0;
        }
        else
        {
            result.close();
            closeConnection();
            return 0;
        }
    }
    
    public long getNextSequenceValue(String name) throws RepositoryException
    {
        getConnection().begin(); //TODO should be IsolationLevels.SERIALIZABLE but not supported by Sesame 2.7
        IRI sequence = RESOURCE.createSequenceURI(name);
        RepositoryResult<Statement> result = getConnection().getStatements(sequence, RDF.VALUE, null, false); 
        long val = 0;
        if (result.hasNext())
        {
            Statement statement = result.next();
            Value vval = statement.getObject();
            if (vval instanceof Literal)
                val = ((Literal) vval).longValue();
            getConnection().remove(statement);
        }
        result.close();
        val++;
        ValueFactory vf = SimpleValueFactory.getInstance();
        getConnection().add(sequence, RDF.VALUE, vf.createLiteral(val));
        getConnection().commit();
        closeConnection();
        return val;
    }
    
    //= Prefixes =======================================================================
    
    protected void initPrefixes()
    {
        addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        addPrefix("b", BOX.NAMESPACE);
        addPrefix("a", SEGM.NAMESPACE);
        addPrefix("fl", FL.NAMESPACE);
        addPrefix("r", RESOURCE.NAMESPACE);
    }
    
    /**
     * Adds a new prefix to be used.
     * @param prefix the prefix string
     * @param uri the corresponding IRI prefix
     */
    public void addPrefix(String prefix, String uri)
    {
        prefixUris.put(prefix, uri);
        uriPrefixes.put(uri, prefix);
    }
    
    /**
     * Gets a map that assigns uris to prefix names.
     * @return the map
     */
    public Map<String, String> getPrefixUris()
    {
        return prefixUris;
    }
    
    /**
     * Gets a map that assigns prefix names to uris.
     * @return the map
     */
    public Map<String, String> getUriPrefixes()
    {
        return uriPrefixes;
    }
    
    /**
     * Gets the prefix declaration string (e.g. for SPARQL) containing the currenly defined prefixes.
     * @return the prefix declaration string
     */
    public String declarePrefixes()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : prefixUris.entrySet())
        {
            sb.append("PREFIX ")
                .append(entry.getKey()).append(": <")
                .append(entry.getValue()).append("> ");
        }
        return sb.toString();
    }
    
    /**
     * Converts an IRI to a prefixed string.
     * @param iri
     * @return
     */
    public String encodeIri(IRI iri)
    {
        String ret = iri.toString();
        for (Map.Entry<String, String> entry : uriPrefixes.entrySet())
        {
            if (ret.startsWith(entry.getKey()))
            {
                ret = ret.replace(entry.getKey(), entry.getValue() + ":");
                break;
            }
        }
        return ret;
    }
    
    /**
     * Converts a prefixed string to an IRI
     * @param shortIri
     * @return
     */
    public IRI decodeIri(String shortIri)
    {
        String ret = shortIri;
        for (Map.Entry<String, String> entry : prefixUris.entrySet())
        {
            if (ret.startsWith(entry.getKey() + ":"))
            {
                ret.replace(entry.getKey() + ":", entry.getValue());
                break;
            }
        }
        ValueFactory vf = SimpleValueFactory.getInstance();
        return vf.createIRI(ret);
    }
}
