/**
 * PDFBoxTreeBuilder.java
 *
 * Created on 12. 10. 2022, 13:02:41 by burgetr
 */
package cz.vutbr.fit.layout.pdf.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class PDFBoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(PDFBoxTreeBuilder.class);

    /** The resulting page */
    private PageImpl page;
    
    /** Acquire images? */
    private boolean acquireImages;
    
    /** Inlcude screen shots? */
    private boolean includeScreenshot;

    
    public PDFBoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }

    public boolean getAcquireImages()
    {
        return acquireImages;
    }

    public void setAcquireImages(boolean acquireImages)
    {
        this.acquireImages = acquireImages;
    }

    public boolean getIncludeScreenshot()
    {
        return includeScreenshot;
    }

    public void setIncludeScreenshot(boolean includeScreenshot)
    {
        this.includeScreenshot = includeScreenshot;
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }

    public void parse(String urlstring) throws MalformedURLException, IOException, SAXException
    {
        parse(new URL(urlstring.trim()));
    }
    
    public void parse(URL url) throws IOException, SAXException
    {
        // TODO
    }
    
    
}
