/**
 * RDFPageSet.java
 *
 * Created on 2. 2. 2016, 0:02:20 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.AbstractPageSet;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.RDFStorage;

/**
 * A page set stored in a RDF repository.
 * 
 * @author burgetr
 */
public class RDFPageSet extends AbstractPageSet
{
    private static Logger log = LoggerFactory.getLogger(RDFPageSet.class);
    
    private RDFStorage storage;
    private IRI iri;

    public RDFPageSet(String name, IRI iri, RDFStorage storage)
    {
        super(name);
        this.iri = iri;
        this.storage = storage;
    }

    @Override
    public int size()
    {
        try
        {
            return storage.getPagesForPageSet(iri).size();
        } catch (RepositoryException e) {
            log.error("Error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Page get(int index) throws IndexOutOfBoundsException
    {
        try
        {
            List<IRI> uris = storage.getPagesForPageSet(iri);
            if (index < uris.size())
                return storage.loadPage(uris.get(index));
            else
                throw new IndexOutOfBoundsException("Page index out of bounds: " + index + " >= " + uris.size()); 
        } catch (RepositoryException e) {
            log.error("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void addPage(Page page)
    {
        try
        {
            if (page instanceof RDFPage)
                storage.addPageToPageSet(((RDFPage) page).getIri(), getName());
            else
                log.error("addPage: The saved instance of the page is required.");
        } 
        catch (RepositoryException e)
        {
            log.error("Error: " + e.getMessage());
        }
    }

    @Override
    public Iterator<Page> iterator()
    {
        try
        {
            return new PageIterator(storage, storage.getPagesForPageSet(iri));
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString()
    {
        return getName();
    }

    public IRI[] getAreaTreeIRIs()
    {
        try
        {
            ArrayList<IRI> list = new ArrayList<IRI>();
            TupleQueryResult data = storage.getAvailableTrees(getName());
            while (data.hasNext())
            {
                BindingSet tuple = data.next();
                if (tuple.getBinding("tree").getValue() instanceof IRI)
                {
                    list.add((IRI) tuple.getBinding("tree").getValue());
                }
            }
            IRI[] ret = new IRI[list.size()];
            return list.toArray(ret);
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public class PageIterator implements Iterator<Page>
    {
        private RDFStorage storage;
        private List<IRI> pageUris;
        private int currentIndex;
        
        public PageIterator(RDFStorage storage, List<IRI> pageIris)
        {
            this.storage = storage;
            this.pageUris = pageIris;
            currentIndex = 0;
        }

        @Override
        public boolean hasNext()
        {
            return (currentIndex < pageUris.size());
        }

        @Override
        public Page next()
        {
            if (currentIndex < pageUris.size())
            {
                try
                {
                    return storage.loadPage(pageUris.get(currentIndex++));
                } catch (RepositoryException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else
                return null;
        }

        @Override
        public void remove()
        {
        }
        
    }
    
}
