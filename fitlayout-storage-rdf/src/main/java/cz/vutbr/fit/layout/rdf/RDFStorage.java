package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides an abstraction of an RDF repository and implements the common
 * low-level operations.
 * 
 * @author burgetr
 */
public class RDFStorage implements Closeable
{
    private static Logger log = LoggerFactory.getLogger(RDFStorage.class);
    
	private Repository repo;

	/**
	 * Use the create functions for creating the instances.
	 */
	protected RDFStorage(Repository repo)
	{
	    this.repo = repo;
	}
	
	public static RDFStorage createMemory(String dataDir)
	{
        log.info("Using memory storage in {}", dataDir);
        final Repository repo;
        if (dataDir != null)
            repo = new SailRepository(new MemoryStore(new File(dataDir)));
        else
            repo = new SailRepository(new MemoryStore());
	    return new RDFStorage(repo);
	}
	
    public static RDFStorage createNative(String dataDir)
    {
        log.info("Using native storage in {}", dataDir);
        final Repository repo = new SailRepository(new NativeStore(new File(dataDir)));
        return new RDFStorage(repo);
    }
    
    public static RDFStorage createHTTP(String serverUrl, String repositoryId)
    {
        log.info("Using HTTP storage in {} : {}", serverUrl, repositoryId);
        final Repository repo = new HTTPRepository(serverUrl, repositoryId);
        return new RDFStorage(repo);
    }
    
    public Repository getRepository()
    {
        return repo;
    }
    
    public RepositoryConnection getConnection()
    {
        return repo.getConnection();
    }

    @Override
    public void close()
    {
        repo.shutDown();
    }
	
    //= Low-level repository functions ==============================================================================

    /**
     * Obtains the value of the given predicate for the given subject.
     * @param subject the subject resource
     * @param predicate the predicate IRI
     * @return the resulting Value or {@code null} when there is no corresponding triplet available.
     * @throws StorageException
     */
    public Value getPropertyValue(Resource subject, IRI predicate) throws StorageException
    {
        Value ret = null;
        try (RepositoryConnection con = repo.getConnection()) {
            RepositoryResult<Statement> result = con.getStatements(subject, predicate, null, true);
            try {
                if (result.hasNext())
                    ret = result.next().getObject();
            }
            finally {
                result.close();
            }
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    /**
     * Obtains a model for the specific subject.
     * @param subject
     * @return
     * @throws StorageException
     */
    public Model getSubjectModel(Resource subject) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final RepositoryResult<Statement> result = con.getStatements(subject, null, null, true);
            return QueryResults.asModel(result);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Obtains a model containing all statements in a given context.
     * 
     * @param context the context IRI
     * @return
     * @throws StorageException
     */
    public Model getContextModel(Resource context) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final RepositoryResult<Statement> result = con.getStatements(null, null, null, context);
            return QueryResults.asModel(result);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Obtains a model containing all statements in a given context.
     * 
     * @param contexts the context IRIs
     * @return
     * @throws StorageException
     */
    public Model getContextModel(Collection<Resource> contexts) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final Resource[] res = contexts.toArray(new Resource[contexts.size()]);
            final RepositoryResult<Statement> result = con.getStatements(null, null, null, res);
            return QueryResults.asModel(result);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Executes an internal (safe) SPARQL graph query.
     * @param query the SPARQL query
     * @return a the resulting model
     * @throws StorageException
     */
    public Model executeSafeQuery(String query) throws StorageException
    {
        try {
            return Repositories.graphQuery(repo, query, r -> QueryResults.asModel(r));
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Executes an internal (safe) tuple query.
     * @param query
     * @return a list of binding sets object representing the result
     * @throws StorageException
     */
    public List<BindingSet> executeSafeTupleQuery(String query) throws StorageException
    {
        try {
            return Repositories.tupleQuery(repo, query, r -> QueryResults.asList(r));
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Inserts a new graph to the database.
     * @param graph
     * @throws StorageException 
     */
    public void insertGraph(Model graph) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.begin();
            con.add(graph);
            con.commit();
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Inserts a new graph to the database.
     * @param graph
     * @param contextIri the context to be used for the inserted statements
     * @throws StorageException 
     */
    public void insertGraph(Model graph, IRI contextIri) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.begin();
            con.add(graph, contextIri);
            con.commit();
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Clears the entire RDF repository.
     */
    public void clear() throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.clear();
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Clears the entire RDF repository.
     */
    public void clear(IRI context) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.clear(context);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    public void execSparqlUpdate(String query) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            Update upd = con.prepareUpdate(QueryLanguage.SPARQL, query);
            upd.execute();
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
    public void importTurtle(String query) throws StorageException, IOException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), "http://fitlayout.github.io/ontology/render.owl#", RDFFormat.TURTLE);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    public void importXML(String query) throws StorageException, IOException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), "http://fitlayout.github.io/ontology/render.owl#", RDFFormat.RDFXML);
        } catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }

    
    //= Sequences ==================================================================
    
    /**
     * Obtains the last assigned value of a sequence with the given name.
     * @param name the sequence name
     * @return the last assigned value or 0 when the sequence does not exist.
     * @throws StorageException 
     */
    public long getLastSequenceValue(String name) throws StorageException
    {
        long ret = 0;
        try (RepositoryConnection con = repo.getConnection()) {
            IRI sequence = RESOURCE.createSequenceURI(name);
            RepositoryResult<Statement> result = con.getStatements(sequence, RDF.VALUE, null, false);
            try {
                if (result.hasNext())
                {
                    Value val = result.next().getObject();
                    if (val instanceof Literal)
                        ret = ((Literal) val).longValue();
                }
            }
            finally {
                result.close();
            }
        }
        catch (RDF4JException e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    public long getNextSequenceValue(String name) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.begin(IsolationLevels.SERIALIZABLE); //TODO is this supported everywhere?
            IRI sequence = RESOURCE.createSequenceURI(name);
            long val = 0;
            RepositoryResult<Statement> result = con.getStatements(sequence, RDF.VALUE, null, false);
            try {
                if (result.hasNext())
                {
                    Statement statement = result.next();
                    Value vval = statement.getObject();
                    if (vval instanceof Literal)
                        val = ((Literal) vval).longValue();
                    con.remove(statement);
                }
            }
            finally {
                result.close();
            }
            val++;
            ValueFactory vf = SimpleValueFactory.getInstance();
            con.add(sequence, RDF.VALUE, vf.createLiteral(val));
            con.commit();
            return val;
        }
        catch (RDF4JException e) {
            throw new StorageException(e);
        }
    }
    
}
