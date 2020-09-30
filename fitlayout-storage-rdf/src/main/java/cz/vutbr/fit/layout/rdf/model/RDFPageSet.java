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
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFPageSetRepository;

/**
 * A page set stored in an artifact repository.
 * 
 * @author burgetr
 */
public class RDFPageSet extends AbstractPageSet
{
    private static Logger log = LoggerFactory.getLogger(RDFPageSet.class);
    
    private RDFArtifactRepository artRepo;
    private RDFPageSetRepository setRepo;
    private IRI iri;

    
    public RDFPageSet(String name, IRI iri, RDFArtifactRepository artRepo, RDFPageSetRepository setRepo)
    {
        super(name);
        this.iri = iri;
        this.artRepo = artRepo;
        this.setRepo = setRepo;
    }

    @Override
    public int size()
    {
        try
        {
            return setRepo.getPagesForPageSet(iri).size();
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
            List<IRI> uris = setRepo.getPagesForPageSet(iri);
            if (index < uris.size())
                return (Page) artRepo.getArtifact(uris.get(index));
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
                setRepo.addPageToPageSet(((RDFPage) page).getIri(), getName());
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
            return new PageIterator(setRepo.getPagesForPageSet(iri));
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
            TupleQueryResult data = setRepo.getAvailableTrees(getName());
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
    
    //====================================================================================================
    
    public class PageIterator implements Iterator<Page>
    {
        private List<IRI> pageUris;
        private int currentIndex;
        
        public PageIterator(List<IRI> pageIris)
        {
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
                    return (Page) artRepo.getArtifact(pageUris.get(currentIndex++));
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
