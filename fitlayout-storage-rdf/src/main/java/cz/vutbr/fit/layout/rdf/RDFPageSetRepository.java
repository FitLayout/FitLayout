/**
 * RDFPageRepository.java
 *
 * Created on 30. 9. 2020, 15:11:34 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

import cz.vutbr.fit.layout.api.PageSet;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.rdf.model.RDFPageSet;

/**
 * A repository of pages and page sets implemented on top of an RDFStorage
 * 
 * @author burgetr
 */
public class RDFPageSetRepository
{
    private RDFArtifactRepository artRepo;
    private RDFStorage storage;

    
    public RDFPageSetRepository(RDFArtifactRepository artRepo)
    {
        this.artRepo = artRepo;
        this.storage = artRepo.getStorage();
    }

    //= Page sets ==============================================================================

    public void createPageSet(String name) throws RepositoryException
    {
        Model graph = new LinkedHashModel(); // it holds whole model
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI uri = RESOURCE.createPageSetURI(name);
        graph.add(uri, RDF.TYPE, FL.PageSet);
        graph.add(uri, FL.hasName, vf.createLiteral(name));
        graph.add(uri, FL.createdOn, vf.createLiteral(new java.util.Date()));
        storage.insertGraph(graph);
    }
    
    public void deletePageSet(String name) throws RepositoryException
    {
        IRI uri = RESOURCE.createPageSetURI(name);
        storage.getConnection().remove(uri, null, null);
        storage.closeConnection();
    }
    
    public PageSet getPageSet(String name) throws RepositoryException
    {
        return getPageSet(RESOURCE.createPageSetURI(name));
    }
    
    public PageSet getPageSet(IRI uri) throws RepositoryException
    {
        RepositoryResult<Statement> result = storage.getConnection().getStatements(uri, null, null, false);
        RDFPageSet ret = new RDFPageSet(null, uri, artRepo, this);
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
        storage.closeConnection();
        if (ret.getName() == null)
            return null; //not found
        else
            return ret;
    }
    
    public List<IRI> getPagesForPageSet(IRI pageSetUri) throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "SELECT ?uri "
                + "WHERE {"
                + "  <" + pageSetUri.toString() + "> layout:containsPage ?uri . "
                + "  ?uri rdf:type box:Page "
                + "}";
        System.out.println("QUERY: " + query);
        TupleQueryResult data = storage.executeSafeTupleQuery(query);
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
        RepositoryResult<Statement> result = storage.getConnection().getStatements(null, RDF.TYPE, FL.PageSet, false);
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
        storage.closeConnection();
        return ret;
    }
    
    public void addPageToPageSet(IRI pageUri, String psetName) throws RepositoryException
    {
        IRI psetUri = RESOURCE.createPageSetURI(psetName);
        storage.getConnection().add(psetUri, FL.containsPage, pageUri);
        storage.closeConnection();
    }
    
    /**
     * Finds all the pages that do not belong to any page set.
     * @return a set of page URIs
     * @throws RepositoryException 
     */
    public Set<IRI> getOrphanedPages() throws RepositoryException
    {
        final String query = storage.declarePrefixes()
                + "SELECT ?pg "
                + "WHERE {"
                + "  ?pg rdf:type box:Page "
                + "  OPTIONAL { ?set layout:containsPage ?pg } "
                + "  FILTER ( !BOUND(?set) ) "
                + "}";
        
        System.out.println("QUERY: " + query);
        TupleQueryResult data = storage.executeSafeTupleQuery(query);
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
    
    //==============================================================================
    
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
        final String query = storage.declarePrefixes()
                + " SELECT ?page ?tree ?date ?url ?title " 
                + "WHERE {"
                +     "?tree segm:sourcePage ?page . " 
                +     "?page box:launchDatetime ?date . "
                +     "?page box:hasTitle ?title . "
                +     "?page box:sourceUrl ?url" + contClause
                + "} ORDER BY ?date ?page ?tree";
        System.out.println("QUERY: " + query);
        return storage.executeSafeTupleQuery(query);
    }
    
}
