/**
 * RDFPage.java
 *
 * Created on 13. 1. 2016, 22:33:22 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultPage;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class RDFPage extends DefaultPage implements RDFResource
{
    protected Date createdOn;
    protected Map<IRI, RDFBox> boxIris;
    

    public RDFPage(URL url)
    {
        super(url);
    }

    public RDFPage(URL url, Date createdOn)
    {
        super(url);
        this.createdOn = createdOn;
    }
    
    public RDFPage(Page src, IRI pageIri)
    {
        super(src);
        setIri(pageIri);
    }

    public Date getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn)
    {
        this.createdOn = createdOn;
    }

    public Map<IRI, RDFBox> getBoxIris()
    {
        return boxIris;
    }

    public void setBoxIris(Map<IRI, RDFBox> boxUris)
    {
        this.boxIris = boxUris;
    }

    public RDFBox findBoxByIri(IRI uri)
    {
        if (boxIris != null)
            return boxIris.get(uri);
        else
            return null;
    }
}
