/**
 * SavedQuery.java
 *
 * Created on 29. 5. 2023, 11:20:16 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;

/**
 * A SPARQL query saved in the repository.
 * 
 * @author burgetr
 */
public class SavedQuery
{
    private IRI iri;
    private String title;
    private String queryString;
    
    public SavedQuery()
    {
    }

    public SavedQuery(String title, String queryString)
    {
        this.title = title;
        this.queryString = queryString;
    }

    public SavedQuery(IRI iri, String title, String queryString)
    {
        this.iri = iri;
        this.title = title;
        this.queryString = queryString;
    }

    public IRI getIri()
    {
        return iri;
    }

    public void setIri(IRI iri)
    {
        this.iri = iri;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

}
