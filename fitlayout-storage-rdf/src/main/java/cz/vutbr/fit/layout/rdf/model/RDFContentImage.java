/**
 * RDFContentImage.java
 *
 * Created on 15. 11. 2016, 13:52:18 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.ContentImage;

/**
 * ContentImage implementation for the RDF model.
 * 
 * @author burgetr
 */
public class RDFContentImage extends RDFContentObject implements ContentImage
{
    private URL url;
    private byte[] pngData;

    public RDFContentImage(IRI iri)
    {
        super(iri);
    }

    @Override
    public URL getUrl()
    {
        return url;
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }
    
    public void setUrl(String url) throws MalformedURLException
    {
        this.url = new URL(url);
    }
    
    @Override
    public byte[] getPngData()
    {
        return pngData;
    }

    public void setPngData(byte[] pngData)
    {
        this.pngData = pngData;
    }

    @Override
    public String toString()
    {
        return "RDFContentImage [url=" + url + ", data="
                + ((pngData == null) ? "null" : (pngData.length + " bytes")) + "]";
    }
    
}
