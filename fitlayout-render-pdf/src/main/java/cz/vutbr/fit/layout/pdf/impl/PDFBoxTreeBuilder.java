/**
 * PDFBoxTreeBuilder.java
 *
 * Created on 12. 10. 2022, 13:02:41 by burgetr
 */
package cz.vutbr.fit.layout.pdf.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.pdfbox.pdmodel.PDDocument;
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
    
    private static String USER_AGENT = "Mozilla/5.0 (compatible; FitLayout/2.x; Linux) PDFRenderer/2.x (like Gecko)";

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
        FLBoxTree boxTree = createBoxTree(url);
        //TODO create page from boxTree
    }
    
    private FLBoxTree createBoxTree(URL url) throws IOException
    {
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);

        pageUrl = con.getURL();
        InputStream is = con.getInputStream();
        String mime = con.getHeaderField("Content-Type");
        if (mime == null)
            mime = "application/pdf";
        int p = mime.indexOf(';');
        if (p != -1)
            mime = mime.substring(0, p).trim();
        log.info("File type: " + mime);
        
        if (mime.equals("application/pdf"))
        {
            PDDocument doc = loadPdf(is);
            FLBoxTree boxTree = new FLBoxTree();
            boxTree.processDocument(doc, 0, Integer.MAX_VALUE);
            doc.close();
            pageTitle = "";
            return boxTree;
        }
        else
        {
            log.error("Unsupported MIME type {}", mime);
            return null;
        }
    }
    
    private PDDocument loadPdf(InputStream is) throws IOException
    {
        PDDocument document = null;
        document = PDDocument.load(is);
        return document;
    }

}
