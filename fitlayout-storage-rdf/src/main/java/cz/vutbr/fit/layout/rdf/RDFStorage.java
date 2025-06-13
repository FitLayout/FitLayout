package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.common.transaction.IsolationLevel;
import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.impl.SimpleBinding;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ServiceConfig;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;


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
     * Finds IRIs of resources of the given type.
     * @param type The resource type IRI.
     * @return a collection of Resources
     */
    public Collection<Resource> getResourcesOfType(IRI type)
    {
        Set<Resource> ret = new HashSet<>(); 
        try (RepositoryConnection con = repo.getConnection()) {
            try (RepositoryResult<Statement> result = con.getStatements(null, RDF.TYPE, type)) {
                for (Statement st : result)
                    ret.add(st.getSubject());
            }
        }
        return ret;
    }
    
    /**
     * Finds IRIs of resources of the given type.
     * @param type The resource type IRI.
     * @param context Repository context
     * @return a collection of Resources
     */
    public Collection<Resource> getResourcesOfType(IRI type, Resource context)
    {
        Set<Resource> ret = new HashSet<>(); 
        try (RepositoryConnection con = repo.getConnection()) {
            try (RepositoryResult<Statement> result = con.getStatements(null, RDF.TYPE, type, context)) {
                for (Statement st : result)
                    ret.add(st.getSubject());
            }
        }
        return ret;
    }
    
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
            try (RepositoryResult<Statement> result = con.getStatements(subject, predicate, null, true)) {
                if (result.hasNext())
                    ret = result.next().getObject();
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
     * Obtains a model for the specific object.
     * @param object
     * @return
     * @throws StorageException
     */
    public Model getObjectModel(Resource object) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final RepositoryResult<Statement> result = con.getStatements(null, null, object, true);
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
            try (final RepositoryResult<Statement> result = con.getStatements(subject, RDF.TYPE, null, true)) {
                for (Statement st : result)
                {
                    if (st.getObject() instanceof IRI)
                        return (IRI) st.getObject(); 
                }
                return null; //no type statement found
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Retrieves all statements where the given resource is a subject or object and creates
     * a list of binding sets that describe the resource. Each binding sets contains a "p"
     * binding for the predicate and "v" for value, where the value is the object when the
     * resource is the subject and vice versa.
     * 
     * @param res the resource to describe
     * @param isSubject <code>true</code> when the resource is the subject, <code>false</code>for object
     * @param limit the maximal number of bindings produced
     * @return A list of binding sets describing the statements (may be empty)
     */
    public SparqlQueryResult.TupleResult describeResource(Resource res, boolean isSubject, long limit)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            List<BindingSet> ret = new ArrayList<>();
            try (final RepositoryResult<Statement> result = isSubject ? con.getStatements(res, null, null, true) : con.getStatements(null, null, res, true)) {
                long cnt = 0;
                for (Statement st : result)
                {
                    MapBindingSet bset = new MapBindingSet(2);
                    bset.addBinding(new SimpleBinding("p", st.getPredicate()));
                    bset.addBinding(new SimpleBinding("v", isSubject ? st.getObject() : st.getSubject()));
                    ret.add(bset);
                    if (++cnt >= limit)
                        break;
                }
            }
            return new SparqlQueryResult.TupleResult(List.of("p", "v"), ret);
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
     * Executes an internal (safe) SPARQL graph query in a default isolation level.
     * @param query the SPARQL query
     * @return a the resulting model
     * @throws StorageException
     */
    public Model executeSafeQuery(String query) throws StorageException
    {
        return executeSafeQuery(query, IsolationLevels.SNAPSHOT_READ);
    }

    /**
     * Executes an internal (safe) SPARQL graph query in a given transaction isolation level.
     * @param query the SPARQL query
     * @param isolationLevel the transaction isolation level or {@code null} when no transaction is required.
     * @return a the resulting model
     * @throws StorageException
     */
    public Model executeSafeQuery(String query, IsolationLevel isolationLevel) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            //return Repositories.graphQuery(repo, query, r -> QueryResults.asModel(r)); // problems with multithreading
            
            if (isolationLevel != null)
                con.begin(isolationLevel);
            GraphQueryResult graphResult = con.prepareGraphQuery(query).evaluate();
            Model ret = new LinkedHashModel();
            for (Statement st: graphResult)
                ret.add(st);
            graphResult.close();
            if (isolationLevel != null)
                con.commit();
            return ret;

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
     * Checks and executes extrenal (possibly unsafe) SPARQL SELECT query and returns a result.
     * This is a special version of {@link #executeSparqlQuery(String, boolean, long, long)}
     * optimized for SELECT queries.
     * @param queryString the SPARQL SELECT query
     * @param distinct {@code true} when only distinct results should be returned
     * @param limit maximal number of returned results
     * @param offset index of the first result to be returned
     * @return a query result that holds the result type and data
     * @throws StorageException when the query could not be parsed or executed or is not a SELECT query.
     */
    public SparqlQueryResult.TupleResult executeSparqlTupleQuery(String queryString, boolean distinct, long limit, long offset) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            TupleQuery query = con.prepareTupleQuery(queryString);
            TupleQueryResult result = ((TupleQuery) query).evaluate();
            if (distinct)
                result = QueryResults.distinctResults(result);
            result = QueryResults.limitResults(result, limit, offset);
            return new SparqlQueryResult.TupleResult(result.getBindingNames(), QueryResults.asList(result));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Checks and executes extrenal (possibly unsafe) SPARQL query and returns a result depending
     * on the query type.
     * @param queryString the SPARQL query
     * @param distinct {@code true} when only distinct results should be returned
     * @param limit maximal number of returned results
     * @param offset index of the first result to be returned
     * @return a query result that holds the result type and data
     * @throws StorageException when the query could not be parsed or executed
     */
    public SparqlQueryResult executeSparqlQuery(String queryString, boolean distinct, long limit, long offset) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            Query query = con.prepareQuery(queryString);
            if (query instanceof TupleQuery)
            {
                TupleQueryResult result = ((TupleQuery) query).evaluate();
                if (distinct)
                    result = QueryResults.distinctResults(result);
                result = QueryResults.limitResults(result, limit, offset);
                return SparqlQueryResult.createTuple(result.getBindingNames(), QueryResults.asList(result));
            }
            else if (query instanceof GraphQuery)
            {
                GraphQueryResult result = ((GraphQuery) query).evaluate();
                if (distinct)
                    result = QueryResults.distinctResults(result);
                result = QueryResults.limitResults(result, limit, offset);
                return SparqlQueryResult.createGraph(QueryResults.asList(result));
            }
            else if (query instanceof BooleanQuery)
            {
                boolean result = ((BooleanQuery) query).evaluate();
                return SparqlQueryResult.createBoolean(result);
            }
            else
                throw new StorageException("Unsupported query type" + query.getClass().getName());
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
    
    public void queryExportCSV(String queryString, OutputStream ostream) throws StorageException 
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.prepareTupleQuery(queryString).evaluate(new SPARQLResultsCSVWriter(ostream));
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
            try {
                con.begin();
                con.add(subj, pred, obj, context);
                con.commit();
            } catch (RepositoryException e) {
                con.rollback();
            }
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
            final Value val = createValueFromObject(value, vf);
            con.add(subj, pred, val, context);
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
            try {
                con.begin();
                con.add(graph);
                con.commit();
            } catch (RepositoryException e) {
                con.rollback();
            }
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
            try {
                con.begin();
                con.add(graph, contextIri);
                con.commit();
            } catch (RepositoryException e) {
                con.rollback();
            }
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
    
    public void importStream(InputStream stream, RDFFormat dataFormat) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(stream, IMPORT_BASE_URI, dataFormat);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void importStream(InputStream stream, RDFFormat dataFormat, String baseURI) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(stream, baseURI, dataFormat);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void importStream(InputStream stream, RDFFormat dataFormat, IRI context) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(stream, IMPORT_BASE_URI, dataFormat, context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void importStream(InputStream stream, RDFFormat dataFormat, IRI context, String baseURI) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(stream, baseURI, dataFormat, context);
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
     * Clears the entire context from the RDF repository.
     * @param context Context IRI
     */
    public void clear(IRI context) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.clear(context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    public void removeStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.remove(subj, pred, obj, contexts);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Removes a quadruple from the storage.
     * @param subj
     * @param pred
     * @param obj
     * @param context
     */
    public void remove(IRI subj, IRI pred, IRI obj, IRI context)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.remove(subj, pred, obj, context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Removes a quadruple from the storage.
     * @param subj
     * @param pred
     * @param value
     * @param context
     */
    public void removeValue(IRI subj, IRI pred, Object value, IRI context)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            final ValueFactory vf = getValueFactory();
            final Value val = createValueFromObject(value, vf);
            con.remove(subj, pred, val, context);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    //= Service descr ==============================================================
    
    public ServiceConfig loadServiceConfig(IRI iri) throws StorageException
    {
        Value serviceId = getPropertyValue(iri, FL.service);
        if (serviceId != null && serviceId instanceof Literal)
        {
            Map<String, Object> params = new HashMap<>();
            try (RepositoryConnection con = repo.getConnection()) {
                try (RepositoryResult<Statement> result = con.getStatements(iri, FL.param, null, false)) {
                    while (result.hasNext())
                    {
                        final Value paramNode = result.next().getObject();
                        if (paramNode instanceof Resource)
                        {
                            String paramName = null;
                            Object paramValue = null;
                            try (RepositoryResult<Statement> pstatements = con.getStatements((Resource) paramNode, null, null)) {
                                for (Statement pst : pstatements)
                                {
                                    if (FL.paramName.equals(pst.getPredicate()))
                                    {
                                        final Value val = pst.getObject();
                                        if (val instanceof Literal)
                                            paramName = val.stringValue();
                                    }
                                    else if (FL.paramValue.equals(pst.getPredicate()))
                                    {
                                        final Value val = pst.getObject();
                                        if (val instanceof Literal)
                                            paramValue = getLiteralAsObject((Literal) val);
                                    }
                                }
                            }
                            if (paramName != null && paramValue != null)
                                params.put(paramName, paramValue);
                        }
                    }
                }
            }
            return new ServiceConfig(serviceId.stringValue(), params);
        }
        else // no service ID defined
            return null;
    }
    
    public static Object getLiteralAsObject(Literal lval)
    {
        final IRI type = lval.getDatatype();
        if (XSD.BOOLEAN.equals(type))
            return Boolean.valueOf(lval.booleanValue());
        else if (XSD.DOUBLE.equals(type))
            return Double.valueOf(lval.doubleValue());
        else if (XSD.FLOAT.equals(type))
            return Float.valueOf(lval.floatValue());
        else if (XSD.INT.equals(type) || XSD.INTEGER.equals(type))
            return Integer.valueOf(lval.intValue());
        else
            return lval.stringValue();
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
            try (RepositoryResult<Statement> result = con.getStatements(sequenceIri, RDF.VALUE, null, false)) {
                if (result.hasNext())
                {
                    Value val = result.next().getObject();
                    if (val instanceof Literal)
                        ret = ((Literal) val).longValue();
                }
            }
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
        return ret;
    }
    
    public long getNextSequenceValue(IRI sequenceIri) throws StorageException
    {
        int tries = 100;
        while (tries > 0)
        {
            // the sequence value fails if the transaction fails (e.g. conflict)
            // give it 100 tries and re-throw the exception when it keeps failing
            try {
                return getNextSequenceValueUnsafe(sequenceIri);
            } catch (StorageException e) {
                tries--;
                if (tries == 0)
                    throw e;
            }
        }
        return 0;
    }
    
    private long getNextSequenceValueUnsafe(IRI sequenceIri) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            try {
                con.begin(IsolationLevels.SERIALIZABLE);
                long val = 0;
                try (RepositoryResult<Statement> result = con.getStatements(sequenceIri, RDF.VALUE, null, false)) {
                    if (result.hasNext())
                    {
                        Statement statement = result.next();
                        Value vval = statement.getObject();
                        if (vval instanceof Literal)
                            val = ((Literal) vval).longValue();
                        con.remove(statement);
                    }
                }
                val++;
                con.add(sequenceIri, RDF.VALUE, getValueFactory().createLiteral(val));
                con.commit();
                return val;
            } catch (RepositoryException e) {
                con.rollback();
                throw new StorageException(e);
            }
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    public void setSequenceValue(IRI sequenceIri, long val) throws StorageException
    {
        try (RepositoryConnection con = repo.getConnection()) {
            con.remove(sequenceIri, RDF.VALUE, null);
            con.add(sequenceIri, RDF.VALUE, getValueFactory().createLiteral(val));
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
            try (RepositoryResult<Namespace> result = con.getNamespaces()) {
                while (result.hasNext())
                {
                    ret.add(result.next());
                }
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
    
    public List<Resource> getContexts()
    {
        List<Resource> ret = new ArrayList<>();
        try (RepositoryConnection con = repo.getConnection()) {
            try (RepositoryResult<Resource> result = con.getContextIDs()) {
                while (result.hasNext())
                {
                    ret.add(result.next());
                }
            }
        }
        catch (Exception e) {
            throw new StorageException(e);
        }
        return ret;
    }

    //=========================================================================================
    
    private Value createValueFromObject(Object value, final ValueFactory vf)
    {
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
        return val;
    }

}
