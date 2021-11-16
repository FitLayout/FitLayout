package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
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

import cz.vutbr.fit.layout.ontology.BOX;


/**
 * This class provides an abstraction of an RDF repository and implements the common
 * low-level operations.
 * 
 * @author burgetr
 */
public class RDFStorage implements Closeable
{
    private static Logger log = LoggerFactory.getLogger(RDFStorage.class);
    
    private static final String IMPORT_BASE_URI = BOX.NAMESPACE;

	private Repository repo;

	/**
	 * Use the create functions for creating the instances.
	 */
	protected RDFStorage(Repository repo)
	{
	    this.repo = repo;
	}
	
	public static RDFStorage create(Repository repo)
	{
	    return new RDFStorage(repo);
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
    
    public ValueFactory getValueFactory()
    {
        return repo.getValueFactory();
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Determines the type of the subject as determined by the corresponding rdf:type predicate (if present)
     * @param subject the subject IRI
     * @return the type IRI or {@code null} when the type is not defined
     * @throws StorageException
     */
    public IRI getSubjectType(Resource subject) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final RepositoryResult<Statement> result = con.getStatements(subject, RDF.TYPE, null, true);
            for (Statement st : result)
            {
                if (st.getObject() instanceof IRI)
                    return (IRI) st.getObject(); 
            }
            return null; //no type statement found
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Adds a new quadruple to the storage.
     * @param subj
     * @param pred
     * @param obj
     * @param context
     */
    public void add(IRI subj, IRI pred, IRI obj, IRI context)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.begin();
            con.add(subj, pred, obj, context);
            con.commit();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Adds a new data quadruple to the storage.
     * @param subj
     * @param pred
     * @param value
     * @param context
     */
    public void addValue(IRI subj, IRI pred, Object value, IRI context)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final ValueFactory vf = getValueFactory();
            final Value val;
            if (value instanceof Integer)
                val = vf.createLiteral((int) value);
            else if (value instanceof Long)
                val = vf.createLiteral((long) value);
            else if (value instanceof Float)
                val = vf.createLiteral((float) value);
            else if (value instanceof Double)
                val = vf.createLiteral((double) value);
            else if (value instanceof Boolean)
                val = vf.createLiteral((boolean) value);
            else
                val = vf.createLiteral(value.toString());
            con.begin();
            con.add(subj, pred, val, context);
            con.commit();
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    public void execSparqlUpdate(String query) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            Update upd = con.prepareUpdate(QueryLanguage.SPARQL, query);
            upd.execute();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void importTurtle(String query) throws StorageException, IOException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), IMPORT_BASE_URI, RDFFormat.TURTLE);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    public void importTurtle(String query, IRI context) throws StorageException, IOException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), IMPORT_BASE_URI, RDFFormat.TURTLE, context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    public void importXML(String query) throws StorageException, IOException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), IMPORT_BASE_URI, RDFFormat.RDFXML);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    public void importXML(String query, IRI context) throws StorageException, IOException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(new StringReader(query), IMPORT_BASE_URI, RDFFormat.RDFXML, context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void queryExportCSV(String queryString, OutputStream ostream) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.prepareTupleQuery(queryString).evaluate(new SPARQLResultsCSVWriter(ostream));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    //= Sequences ==================================================================
    
    /**
     * Obtains the last assigned value of a sequence with the given name.
     * @param sequenceIri the sequence IRI
     * @return the last assigned value or 0 when the sequence does not exist.
     * @throws StorageException 
     */
    public long getLastSequenceValue(IRI sequenceIri) throws StorageException
    {
        long ret = 0;
        try (RepositoryConnection con = repo.getConnection()) {
            RepositoryResult<Statement> result = con.getStatements(sequenceIri, RDF.VALUE, null, false);
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
        catch (Exception e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    public long getNextSequenceValue(IRI sequenceIri) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.begin(IsolationLevels.SERIALIZABLE); //TODO is this supported everywhere?
            long val = 0;
            RepositoryResult<Statement> result = con.getStatements(sequenceIri, RDF.VALUE, null, false);
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
            con.add(sequenceIri, RDF.VALUE, getValueFactory().createLiteral(val));
            con.commit();
            return val;
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    //= Namespaces ==================================================================
    
    public List<Namespace> getNamespaces()
    {
        List<Namespace> ret = new ArrayList<>();
        try (RepositoryConnection con = repo.getConnection()) {
            RepositoryResult<Namespace> result = con.getNamespaces();
            try {
                while (result.hasNext())
                {
                    ret.add(result.next());
                }
            }
            finally {
                result.close();
            }
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    public String getNamespace(String prefix)
    {
        String ret = null;
        try (RepositoryConnection con = repo.getConnection()) {
            ret = con.getNamespace(prefix);
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    public void addNamespace(String prefix, String namespace)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.setNamespace(prefix, namespace);
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void deleteNamespace(String prefix)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.removeNamespace(prefix);
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void clearNamespaces()
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.clearNamespaces();
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
}
