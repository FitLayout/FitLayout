package cz.vutbr.fit.layout.rdf;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
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

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.PageSet;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.io.RDFConnector;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorHTTP;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorMemory;
import cz.vutbr.fit.layout.rdf.io.RDFConnectorNative;
import cz.vutbr.fit.layout.rdf.model.RDFPage;
import cz.vutbr.fit.layout.rdf.model.RDFPageSet;


/**
 * This class implements all the high level operations on a RDF repository.
 * 
 * @author milicka
 * @author burgetr
 */
public class RDFStorage implements ArtifactRepository, Closeable
{
    private static Logger log = LoggerFactory.getLogger(RDFStorage.class);
    
    private static final String PREFIXES =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
            "PREFIX box: <" + BOX.NAMESPACE + "> " +        
            "PREFIX segm: <" + SEGM.NAMESPACE + "> " +
            "PREFIX layout: <" + FL.NAMESPACE + ">";

    private Map<IRI, ModelBuilder> modelBuilders;
    
	private RDFConnector db;

	/**
	 * Use the create functions for creating the instances
	 * @throws RepositoryException
	 */
	protected RDFStorage(RDFConnector connector) throws RepositoryException
	{
	    db = connector;
	    modelBuilders = new HashMap<>();
	    initDefaultModelBuilders();
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
	protected RepositoryConnection getConnection() throws RepositoryException 
	{
		return db.getConnection();
	}
	
	public void closeConnection() throws RepositoryException
	{
	    db.closeConnection();
	}
	
    //Model builders =================================================================

	protected void initDefaultModelBuilders()
	{
	    addModelBuilder(BOX.Page, new BoxModelBuilder());
	    addModelBuilder(SEGM.AreaTree, new AreaModelBuilder());
	    addModelBuilder(SEGM.LogicalAreaTree, new LogicalAreaModelBuilder());
	}
	
	public void addModelBuilder(IRI artifactType, ModelBuilder builder)
	{
	    modelBuilders.put(artifactType, builder);
	}
	
	public ModelBuilder getModelBuilder(IRI artifactType)
	{
	    return modelBuilders.get(artifactType);
	}
	
	//Artifact functions =============================================================
	
    public Collection<IRI> getArtifactIRIs() throws RepositoryException
    {
        final String query = PREFIXES
                + "SELECT ?pg "
                + "WHERE {"
                + "  ?pg rdf:type ?type . "
                + "  ?type rdfs:subClassOf layout:Artifact "
                + "}";
        
        log.debug("QUERY: {}", query);
        TupleQueryResult data = executeSafeTupleQuery(query);
        Set<IRI> ret = new HashSet<IRI>();
        try
        {
            while (data.hasNext())
            {
                BindingSet binding = data.next();
                Binding b = binding.getBinding("pg");
                ret.add((IRI) b.getValue());
            }
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return ret;
    }
	
	@Override
    public Artifact getArtifact(IRI artifactIri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addArtifact(Artifact artifact)
    {
        if (artifact.getIri() == null)
            artifact.setIri(createArtifactIri(artifact));
        
        log.debug("STORING {}", artifact);
        ModelBuilder builder = getModelBuilder(artifact.getArtifactType());
        if (builder != null)
        {
            Model graph = builder.createGraph(artifact);
            insertGraph(graph, artifact.getIri());
        }
        else
            log.error("Could not find RDF model builder for artifact {}, type {}", artifact, artifact.getArtifactType());
    }

    @Override
    public IRI createArtifactIri(Artifact artifact)
    {
        long seq = getNextSequenceValue("page");
        IRI pageUri = RESOURCE.createArtifactIri(seq);
        return pageUri;
    }

    //==============================================================================
    
    public void createPageSet(String name) throws RepositoryException
	{
        Model graph = new LinkedHashModel(); // it holds whole model
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI uri = RESOURCE.createPageSetURI(name);
        graph.add(uri, RDF.TYPE, FL.PageSet);
        graph.add(uri, FL.hasName, vf.createLiteral(name));
        graph.add(uri, FL.createdOn, vf.createLiteral(new java.util.Date()));
	    insertGraph(graph);
	}
	
	public void deletePageSet(String name) throws RepositoryException
	{
	    IRI uri = RESOURCE.createPageSetURI(name);
	    getConnection().remove(uri, null, null);
	    closeConnection();
	}
	
    public PageSet getPageSet(String name) throws RepositoryException
    {
        return getPageSet(RESOURCE.createPageSetURI(name));
    }
    
	public PageSet getPageSet(IRI uri) throws RepositoryException
	{
        RepositoryResult<Statement> result = getConnection().getStatements(uri, null, null, false);
        RDFPageSet ret = new RDFPageSet(null, uri, this);
        while (result.hasNext()) 
        {
            Statement st = result.next();
            if (FL.hasName.equals(st.getPredicate()))
                ret.setName(st.getObject().stringValue());
            else if (FL.createdOn.equals(st.getPredicate()))
            {
                Value val = st.getObject();
                if (val instanceof Literal)
                {
                    Date date = ((Literal) val).calendarValue().toGregorianCalendar().getTime();
                    ret.setDateCreated(date);
                }
            }
        }
        result.close();
        closeConnection();
        if (ret.getName() == null)
            return null; //not found
        else
            return ret;
	}
	
	public List<IRI> getPagesForPageSet(IRI pageSetUri) throws RepositoryException
	{
        final String query = PREFIXES
                + "SELECT ?uri "
                + "WHERE {"
                + "  <" + pageSetUri.toString() + "> layout:containsPage ?uri . "
                + "  ?uri rdf:type box:Page "
                + "}";
        System.out.println("QUERY: " + query);
        TupleQueryResult data = executeSafeTupleQuery(query);
        List<IRI> ret = new ArrayList<IRI>();
        try
        {
            while (data.hasNext())
            {
                BindingSet binding = data.next();
                Binding b = binding.getBinding("uri");
                ret.add((IRI) b.getValue());
            }
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return ret;
	}
	
    /**
     * Reads all the existing page sets.
     * @return a list of page sets
     * @throws RepositoryException 
     */
	public List<PageSet> getPageSets() throws RepositoryException 
    {
        List<PageSet> ret = new ArrayList<PageSet>();
        RepositoryResult<Statement> result = getConnection().getStatements(null, RDF.TYPE, FL.PageSet, false);
        while (result.hasNext()) 
        {
            Statement st = result.next();
            if (st.getSubject() instanceof IRI)
            {
                PageSet newset = getPageSet((IRI) st.getSubject());
                if (newset != null)
                    ret.add(newset);
            }
            
        }
        result.close();
        closeConnection();
        return ret;
    }
	
	public void addPageToPageSet(IRI pageUri, String psetName) throws RepositoryException
	{
	    IRI psetUri = RESOURCE.createPageSetURI(psetName);
	    getConnection().add(psetUri, FL.containsPage, pageUri);
	    closeConnection();
	}
	
	/**
	 * Finds all the pages that do not belong to any page set.
	 * @return a set of page URIs
	 * @throws RepositoryException 
	 */
	public Set<IRI> getOrphanedPages() throws RepositoryException
	{
        final String query = PREFIXES
                + "SELECT ?pg "
                + "WHERE {"
                + "  ?pg rdf:type box:Page "
                + "  OPTIONAL { ?set layout:containsPage ?pg } "
                + "  FILTER ( !BOUND(?set) ) "
                + "}";
        
        System.out.println("QUERY: " + query);
        TupleQueryResult data = executeSafeTupleQuery(query);
        Set<IRI> ret = new HashSet<IRI>();
        try
        {
            while (data.hasNext())
            {
                BindingSet binding = data.next();
                Binding b = binding.getBinding("pg");
                ret.add((IRI) b.getValue());
            }
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
	    return ret;
	}
	
	/**
	 * Removes all the pages that do not belong to any page set. Also removes the corresponding area trees.
	 * @throws RepositoryException
	 */
	public void removeOrphanedPages() throws RepositoryException
	{
	    Set<IRI> pages = getOrphanedPages();
	    for (IRI page : pages)
	        removePage(page);
	}
	
	/**
	 * Obtains the tabular data about the available segmented pages in the repository.
	 * @param psetName the selected page set or {@code null} for all the available pages
	 * @return
	 * @throws RepositoryException
	 */
	public TupleQueryResult getAvailableTrees(String psetName) throws RepositoryException
	{
	    String contClause = "";
	    if (psetName != null)
	    {
	        IRI pageSetUri = RESOURCE.createPageSetURI(psetName);
	        contClause = " . <" + pageSetUri.toString() + "> layout:containsPage ?page";
	    }
	    final String query = PREFIXES
	            + " SELECT ?page ?tree ?date ?url ?title " 
                + "WHERE {"
                +     "?tree segm:sourcePage ?page . " 
                +     "?page box:launchDatetime ?date . "
                +     "?page box:hasTitle ?title . "
                +     "?page box:sourceUrl ?url" + contClause
                + "} ORDER BY ?date ?page ?tree";
	    System.out.println("QUERY: " + query);
	    return executeSafeTupleQuery(query);
	}
	
	//box tree functions ===========================================================	
	
	/**
	 * Returns a list of distinct source urls in database.
	 * @throws RepositoryException 
	 */
	public Set<String> getDistinctPageUrls() throws RepositoryException 
	{
		Set<String> output = new HashSet<String>();

		RepositoryResult<Statement> result = getConnection().getStatements(null, BOX.sourceUrl, null, true);
		while (result.hasNext()) 
		{
			Statement bindingSet = result.next();
			String url = bindingSet.getObject().stringValue();
			output.add(url);
		}
		result.close();
		closeConnection();
		
		return output;
	}

	/**
	 * Obtains the URIs of all the stored Page objects.
	 * @return
	 * @throws RepositoryException 
	 */
	public Set<IRI> getAllPageIds() throws RepositoryException 
	{
		RepositoryResult<Statement> result = getConnection().getStatements(null, RDF.TYPE, BOX.Page, true);
		Set<IRI> ret = getSubjectsFromResult(result);
		result.close();
		closeConnection();
		return ret;
	}
		
	/**
	 * Obtains the pageIDs for a specific url
	 * @param url the processed page url
	 * @return list of launch URIs
	 * @throws RepositoryException 
	 */
	public Set<IRI> getPageIdsForUrl(String url) throws RepositoryException 
	{
		ValueFactory vf = SimpleValueFactory.getInstance(); 
		RepositoryResult<Statement> result = getConnection().getStatements(null, BOX.sourceUrl, vf.createLiteral(url), true); 
		Set<IRI> ret = getSubjectsFromResult(result);
		result.close();
		closeConnection();
		return ret;
	}
	
	/**
	 * Removes a page from the storage. 
	 * @param pageUri the page IRI
	 * @throws RepositoryException 
	 */
	public void removePage(IRI pageUri) throws RepositoryException 
	{
	    removeAreaTreesForPage(pageUri);
		removePageModel(pageUri);
		removePageInfo(pageUri);
	}

	/**
	 * Loads a page from the repository.
	 * @param pageId The page IRI
	 * @return the corresponding Page or {@code null} when the page is not available in the repository.
	 * @throws RepositoryException
	 */
	public RDFPage loadPage(IRI pageId) throws RepositoryException
	{
        BoxModelLoader loader = new BoxModelLoader(this, pageId);
        return loader.getPage();
	}
	
	/**
	 * Gets page box model from the unique page ID.
	 * @param pageId
	 * @return
	 * @throws RepositoryException 
	 */
	public Model getBoxModelForPage(IRI pageId) throws RepositoryException
	{
		final String query = PREFIXES
				+ "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
				+ "?s rdf:type box:Box . "
				+ "?s box:documentOrder ?ord . "
				+ "?s box:belongsTo <" + pageId.toString() + ">}"
				+ " ORDER BY ?ord";
        return executeSafeQuery(query);
	}
	
    /**
     * Gets page box model from the unique page ID.
     * @param pageId
     * @return
     * @throws RepositoryException 
     */
    public Model getBorderModelForPage(IRI pageId) throws RepositoryException
    {
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type box:Box . " 
                + "?b box:belongsTo <" + pageId.toString() + "> . "
                + "{?b box:hasTopBorder ?s} UNION {?b box:hasRightBorder ?s} UNION {?b box:hasBottomBorder ?s} UNION {?b box:hasLeftBorder ?s}}";
        return executeSafeQuery(query);
    }
    
    /**
     * Gets page attribute model from the unique page ID.
     * @param pageId
     * @return
     * @throws RepositoryException 
     */
    public Model getAttributeModelForPage(IRI pageId) throws RepositoryException
    {
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type box:Box . " 
                + "?b box:belongsTo <" + pageId.toString() + "> . "
                + "?b box:hasAttribute ?s}";
        return executeSafeQuery(query);
    }
    
	/**
	 * Loads the page info - sourceUrl, launchDateTime, ...
	 * @param pageId
	 * @return
	 * @throws RepositoryException 
	 */
	public Model getPageInfo(IRI pageUri) throws RepositoryException 
	{
		RepositoryResult<Statement> result = null;
		result = getConnection().getStatements(pageUri, null, null, true);
		Model ret = createModel(result);
		result.close();
		closeConnection();
		return ret;
	}

	
	//AREA tree functions ===========================================================
	
    /**
     * Loads an area tree from the repository.
     * @param areaTreeId The area tree IRI
     * @return a create model loader. Use its {@code getAreaTree} and {@code getLogicalAreaTree}
     * methods for obtaining the trees.
     * @throws RepositoryException
     */
    public AreaModelLoader loadAreaTrees(IRI areaTreeId, IRI srcPageIri) throws RepositoryException
    {
        return new AreaModelLoader(this, areaTreeId, srcPageIri);
    }
    
    /**
     * Obtains the the IRI of the source page given an AreaTree IRI
     * @param areaTreeUri the IRI of the area tree
     * @return the source Page IRI or {@code null} when not specified
     * @throws RepositoryException 
     */
    public IRI getSourcePageForAreaTree(IRI areaTreeUri) throws RepositoryException 
    {
        IRI ret = null;
        RepositoryResult<Statement> result = getConnection().getStatements(areaTreeUri, SEGM.hasSourcePage, null, true); 
        while (result.hasNext())
        {
            Value val = result.next().getObject();
            if (val instanceof IRI)
            {
                ret = (IRI) val;
                break;
            }
        }
        result.close();
        closeConnection();
        return ret;
    }
    
	/**
	 * Obtains the model of visual areas for the given area tree.
	 * @param areaTreeUri
	 * @return A Model containing the triplets for all the visual areas contained in the given area tree.
	 * @throws RepositoryException 
	 */
	public Model getAreaModelForAreaTree(IRI areaTreeUri) throws RepositoryException
	{
		final String query = PREFIXES
				+ "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
				+ "?s rdf:type segm:Area . "
                + "?s box:documentOrder ?ord . "
				+ "?s segm:belongsTo <" + areaTreeUri.stringValue() + "> }"
				+ " ORDER BY ?ord";
        return executeSafeQuery(query);
	}
	
    /**
     * Obtains the model of logical areas for the given area tree.
     * @param areaTreeUri
     * @return A Model containing the triplets for all the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    public Model getLogicalAreaModelForAreaTree(IRI areaTreeUri) throws RepositoryException
    {
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?s rdf:type segm:LogicalArea . "
                + "?s box:documentOrder ?ord . "
                + "?s segm:belongsTo <" + areaTreeUri.stringValue() + "> }"
                + " ORDER BY ?ord";
        return executeSafeQuery(query);
    }
    
    /**
     * Gets page border information for the given area tree.
     * @param pageId
     * @return
     * @throws RepositoryException 
     */
    public Model getBorderModelForAreaTree(IRI areaTreeUri) throws RepositoryException
    {
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . "
                + "?b rdf:type segm:Area . " 
                + "?b segm:belongsTo <" + areaTreeUri.toString() + "> . "
                + "{?b box:hasTopBorder ?s} UNION {?b box:hasRightBorder ?s} UNION {?b box:hasBottomBorder ?s} UNION {?b box:hasLeftBorder ?s}}";
        return executeSafeQuery(query);
    }
    
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeUri
     * @return A Model containing the triplets for all tags of the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
	public Model getTagModelForAreaTree(IRI areaTreeUri) throws RepositoryException
	{
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "{?a rdf:type segm:Area} UNION {?a rdf:type segm:LogicalArea} . "
                + "?a segm:hasTag ?s . "
                + "?a segm:belongsTo <" + areaTreeUri.stringValue() + "> }";
        return executeSafeQuery(query);
	}
	
    /**
     * Obtains the model of visual areas for the given area tree.
     * @param areaTreeUri
     * @return A Model containing the triplets for all tags of the visual areas contained in the given area tree.
     * @throws RepositoryException 
     */
    public Model getTagSupportModelForAreaTree(IRI areaTreeUri) throws RepositoryException
    {
        final String query = PREFIXES
                + "CONSTRUCT { ?s ?p ?o } " + "WHERE { ?s ?p ?o . "
                + "?a rdf:type segm:Area . "
                + "?a segm:tagSupport ?s . "
                + "?a segm:belongsTo <" + areaTreeUri.stringValue() + "> }";
        return executeSafeQuery(query);
    }
    
	/**
	 * Obtains all the area trees for a page IRI.
	 * @param pageUri the IRI of a Page
	 * @return A set of AreaTree URIs that come from the specified page.
	 * @throws RepositoryException 
	 */
	public Set<IRI> getAreaTreeIdsForPageId(IRI pageUri) throws RepositoryException 
	{
		RepositoryResult<Statement> result = getConnection().getStatements(null, SEGM.hasSourcePage, pageUri, true);
        Set<IRI> ret = getSubjectsFromResult(result);
        result.close();
        closeConnection();
        return ret;
	}
	
	/**
	 * Removes the area tree from the repository. 
	 * @param areaTreeUri the IRI of the area tree
	 * @throws RepositoryException
	 */
	public void removeAreaTree(IRI areaTreeUri) throws RepositoryException
	{
        Model mat = getAreaModelForAreaTree(areaTreeUri);
        mat.addAll(getLogicalAreaModelForAreaTree(areaTreeUri));
        mat.addAll(getBorderModelForAreaTree(areaTreeUri));
        //mat.addAll(getTagModelForAreaTree(areaTreeUri)); //tags probably should not be deleted
        mat.addAll(getTagSupportModelForAreaTree(areaTreeUri));
        RepositoryResult<Statement> result = getConnection().getStatements(areaTreeUri, null, null, false);
        while (result.hasNext())
            mat.add(result.next());
        getConnection().remove(mat);
        closeConnection();
	}
	
	//others =========================================================================
	
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

    //sequences ==================================================================
    
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
    
    
	
	//PRIVATE =========================================
	
	/**
	 * Removes the page contents.
	 * @param pageId
	 * @throws RepositoryException 
	 */
	private void removePageModel(IRI pageUri) throws RepositoryException 
	{
        Model mat = getBoxModelForPage(pageUri);
        mat.addAll(getBorderModelForPage(pageUri));
        getConnection().remove(mat);
        closeConnection();
	}

	/**
	 * Removes all the area trees that belong to the given page
	 * @param pageUri
	 * @throws RepositoryException
	 */
	private void removeAreaTreesForPage(IRI pageUri) throws RepositoryException
	{
        //load all area trees
        Set<IRI> areaTreeModels = getAreaTreeIdsForPageId(pageUri);
        //removes all area trees
        for(IRI areaTreeId : areaTreeModels)
            removeAreaTree(areaTreeId);
	}
	
	/**
	 * Removes the page record.
	 * @param pageId
	 * @throws RepositoryException 
	 */
	private void removePageInfo(IRI pageUri) throws RepositoryException 
	{
		Model m = getPageInfo(pageUri);
		getConnection().remove(m);
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
	private TupleQueryResult executeSafeTupleQuery(String query) throws RepositoryException
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
    private void insertGraph(Model graph) throws RepositoryException
	{
        getConnection().begin();
		getConnection().add(graph);
		getConnection().commit();
		closeConnection();
	}

    private void insertGraph(Model graph, IRI contextIri) throws RepositoryException
    {
        getConnection().begin();
        getConnection().add(graph, contextIri);
        getConnection().commit();
        closeConnection();
    }
    
}
