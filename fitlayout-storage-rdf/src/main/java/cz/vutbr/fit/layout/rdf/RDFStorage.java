package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
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
	 * Obtains all statements in a given context.
	 * 
	 * @param context the context IRI
	 * @return
	 * @throws RepositoryException
	 */
	public RepositoryResult<Statement> getContextStatements(Resource context) throws RepositoryException
	{
	    return getConnection().getStatements(null, null, null, context);
	}
	
    /**
     * Obtains a model containing all statements in a given context.
     * 
     * @param context the context IRI
     * @return
     * @throws RepositoryException
     */
    public Model getContextModel(Resource context) throws RepositoryException
    {
        return createModel(getContextStatements(context));
    }
    
    /**
     * Obtains all statements in a given contexts.
     * 
     * @param contexts the context IRIs
     * @return
     * @throws RepositoryException
     */
    public RepositoryResult<Statement> getContextStatements(Collection<Resource> contexts) throws RepositoryException
    {
        final Resource[] res = contexts.toArray(new Resource[contexts.size()]);
        return getConnection().getStatements(null, null, null, res);
    }
    
    /**
     * Obtains a model containing all statements in a given context.
     * 
     * @param contexts the context IRIs
     * @return
     * @throws RepositoryException
     */
    public Model getContextModel(Collection<Resource> contexts) throws RepositoryException
    {
        return createModel(getContextStatements(contexts));
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
     * Obtains a model for the specific subjects.
     * @param subjects
     * @return
     * @throws RepositoryException 
     */
    public Model getSubjectModel(Collection<IRI> subjects) throws RepositoryException 
    {
        Model model = new LinkedHashModel();
        for (Resource subject : subjects)
        {
            RepositoryResult<Statement> result = getSubjectStatements(subject);
            while (result.hasNext())
                model.add(result.next());
            result.close();
        }
        closeConnection();
        return model;
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
	
	/**
	 * Clears the entire RDF repository.
	 */
	public void clear() throws RepositoryException
	{
	    getConnection().clear();
	    closeConnection();
	}
	
	/**
	 * Clears a given context from the repository.
	 * @param context the context IRI to be cleared
	 */
	public void clear(IRI context) throws RepositoryException
	{
	    getConnection().clear(context);
	    closeConnection();
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
	public Model executeSafeQuery(String query) throws RepositoryException
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
    
}
