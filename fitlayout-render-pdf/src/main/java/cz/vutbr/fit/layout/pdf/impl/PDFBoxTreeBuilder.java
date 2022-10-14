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
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
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
    
    private float zoom = 1.0f;
    private int startPage = 0;
    private int endPage = 1000;

    
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
    
    public float getZoom()
    {
        return zoom;
    }

    public void setZoom(float zoom)
    {
        this.zoom = zoom;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public int getEndPage()
    {
        return endPage;
    }

    public void setEndPage(int endPage)
    {
        this.endPage = endPage;
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
        
        List<Box> boxlist = boxTree.getAllBoxes();
        Color bg = Color.WHITE; //TODO detect background color?
        Box root = buildTree(boxlist, bg);
        
        //wrap the root box with a page
        PageImpl pg = page = new PageImpl(pageUrl);
        pg.setTitle(pageTitle);
        pg.setRoot(root);
        pg.setWidth(root.getWidth());
        pg.setHeight(root.getHeight());
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
            boxTree.setZoom(getZoom());
            boxTree.processDocument(doc, getStartPage(), getEndPage());
            doc.close();
            pageTitle = doc.getDocumentInformation().getTitle();
            if (pageTitle == null)
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
