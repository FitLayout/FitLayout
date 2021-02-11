/**
 * CSSBoxTreeBuilder.java
 *
 * Created on 24. 10. 2014, 23:52:25 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.rdf4j.model.IRI;
import org.fit.cssbox.awt.GraphicsEngine;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.Engine;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.pdf.PdfEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.DisplayType;
import cz.vutbr.web.css.MediaSpec;

/**
 * This class implements building the box tree using the CSSBox rendering engine.
 * 
 * @author burgetr
 */
public class CSSBoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(CSSBoxTreeBuilder.class);

    protected URL pageUrl;
    protected String pageTitle;
    
    /** The resulting page */
    protected PageImpl page;
    
    /** CSSBox viewport that represents the rendered page box tree */
    protected Viewport viewport;
    
    /** Requested page dimensions */
    protected Dimension pageSize;
    
    /** Replace the images with their {@code alt} text */
    protected boolean replaceImagesWithAlt;
    
    /** Acquire images? */
    private boolean acquireImages;
    
    /** Inlcude screen shots? */
    private boolean includeScreenshot;
    
    private float zoom;
    
   
    public CSSBoxTreeBuilder(Dimension pageSize, boolean useVisualBounds, boolean preserveAux, boolean replaceImagesWithAlt)
    {
        super(useVisualBounds, preserveAux);
        this.pageSize = pageSize;
        this.useVisualBounds = useVisualBounds;
        this.preserveAux = preserveAux;
        this.replaceImagesWithAlt = replaceImagesWithAlt;
        this.includeScreenshot = true;
        this.zoom = 1.0f;
    }
    
    public float getZoom()
    {
        return zoom;
    }

    public void setZoom(float zoom)
    {
        this.zoom = zoom;
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

    public void setPageIri(IRI pageIri)
    {
        if (page != null)
            page.setIri(pageIri);
    }

    public void parse(URL url) throws IOException, SAXException
    {
        //render the page
        Engine engine = renderUrl(url, pageSize);
        viewport = engine.getViewport();
        PageImpl pg = page = new PageImpl(pageUrl);
        pg.setTitle(pageTitle);
        
        //add the screenshot
        if (includeScreenshot)
        {
            BufferedImage img = ((GraphicsEngine) engine).getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            pg.setPngImage(baos.toByteArray());
        }
        
        //construct the box tree
        Viewport rootbox = engine.getViewport();
        Box root = buildTree(rootbox);
        
        //initialize the page
        pg.setRoot(root);
        pg.setWidth(root.getWidth());
        pg.setHeight(root.getHeight());
    }
    
    public void parseList(List<URL> list) throws IOException, SAXException
    {
        int twidth = 0;
        int theight = 0;

        page = new PageImpl(list.get(0));
        DefaultBox main = new DefaultBox();
        main.setPageIri(page.getIri());
        main.setTagName("pageset");
        
        for (URL url : list)
        {
            log.info("Parsing: {}", url);
            //render the page using custom renderer
            Engine engine = renderUrl(url, pageSize);
            Viewport rootbox = engine.getViewport();
            Box root = buildTree(rootbox);
            
            //wrap the page with a new block box
            DefaultBox pageBox = new DefaultBox();
            pageBox.setPageIri(page.getIri());
            pageBox.appendChild(root);
            pageBox.setTagName("page");
            pageBox.setDisplayType(DisplayType.BLOCK);
            pageBox.setBounds(new Rectangular(root.getBounds()));
            pageBox.setVisualBounds(new Rectangular(root.getBounds()));
            pageBox.setContentBounds(new Rectangular(root.getBounds()));
            pageBox.move(0, theight);
            pageBox.setBackgroundSeparated(true);
            
            log.info("Rendered: {}x{}", pageBox.getWidth(), pageBox.getHeight());
            
            //add to the root
            main.appendChild(pageBox);
            twidth = Math.max(twidth, pageBox.getWidth());
            theight = theight + pageBox.getHeight();
        }
        main.setBounds(new Rectangular(0, 0, twidth, theight));
        main.setVisualBounds(new Rectangular(0, 0, twidth, theight));
        
        //initialize the page
        page.setRoot(main);
        page.setWidth(twidth);
        page.setHeight(theight);
    }
    
    public void parse(String urlstring) throws MalformedURLException, IOException, SAXException
    {
        urlstring = urlstring.trim();
        if (urlstring.startsWith("http:") ||
            urlstring.startsWith("https:") ||
            urlstring.startsWith("ftp:") ||
            urlstring.startsWith("file:"))
        {
            parse(new URL(urlstring));
        }
        else if (urlstring.startsWith("list:"))
        {
            List<URL> list = loadList(urlstring.substring(5));
            parseList(list);
        }
        else
            throw new MalformedURLException("Unsupported protocol in " + urlstring);
    }
    
    /**
     * The resulting page model.
     * @return the page
     */
    @Override
    public Page getPage()
    {
        return page;
    }
    
    //===================================================================
    
    protected Engine renderUrl(URL url, Dimension pageSize) throws IOException, SAXException
    {
        DocumentSource src = new DefaultDocumentSource(url);
        pageUrl = src.getURL();
        InputStream is = src.getInputStream();
        String mime = src.getContentType();
        if (mime == null)
            mime = "text/html";
        int p = mime.indexOf(';');
        if (p != -1)
            mime = mime.substring(0, p).trim();
        log.info("File type: " + mime);
        
        if (mime.equals("application/pdf"))
        {
            PDDocument doc = loadPdf(is);
            Engine engine = new PdfEngine(doc, null, pageSize, src.getURL());
            doc.close();
            pageTitle = "";
            return engine;
        }
        else
        {
            DOMSource parser = new DefaultDOMSource(src);
            Document doc = parser.parse();
            pageTitle = findPageTitle(doc);
            
            String encoding = parser.getCharset();
            
            MediaSpec media = new MediaSpec("screen");
            //updateCurrentMedia(media);
            
            DOMAnalyzer da = new DOMAnalyzer(doc, src.getURL());
            if (encoding == null)
                encoding = da.getCharacterEncoding();
            da.setDefaultEncoding(encoding);
            da.setMediaSpec(media);
            da.attributesToStyles();
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.getStyleSheets();
            
            Engine engine = new GraphicsEngine(da.getRoot(), da, src.getURL());
            engine.setAutoMediaUpdate(false);
            engine.getConfig().setLoadImages(acquireImages);
            engine.getConfig().setLoadBackgroundImages(acquireImages);
            engine.getConfig().setLoadFonts(false);
            engine.getConfig().setReplaceImagesWithAlt(replaceImagesWithAlt);
            defineLogicalFonts(engine.getConfig());
            engine.createLayout(pageSize, new Rectangle(pageSize), includeScreenshot);
            
            src.close();

            return engine;
        }
        
    }
    
    /**
     * Sets some common fonts as the defaults for generic font families.
     */
    protected void defineLogicalFonts(BrowserConfig config)
    {
        config.setLogicalFont(BrowserConfig.SERIF, Arrays.asList("Times", "Times New Roman"));
        config.setLogicalFont(BrowserConfig.SANS_SERIF, Arrays.asList("Arial", "Helvetica"));
        config.setLogicalFont(BrowserConfig.MONOSPACE, Arrays.asList("Courier New", "Courier"));
    }
    
    private PDDocument loadPdf(InputStream is) throws IOException
    {
        PDDocument document = null;
        document = PDDocument.load(is);
        return document;
    }
    
    private String findPageTitle(Document doc)
    {
        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() > 0)
        {
            Element head = (Element) heads.item(0);
            NodeList titles = head.getElementsByTagName("title");
            if (titles.getLength() > 0)
            {
                final String ret = titles.item(0).getTextContent();
                return (ret == null) ? null : ret.trim();
            }
        }
        return "";
    }
    
    //===================================================================
    
    protected Box buildTree(Viewport vp)
    {
        //create the working list of nodes
        log.trace("LIST");
        List<Box> boxlist = createBoxList(vp);
        Color bg = Units.toColor(vp.getBgcolor());
        if (bg == null) bg = Color.WHITE;
        Box root = buildTree(boxlist, bg);
        return root;
    }
    
    /**
     * Creates a list of all the visible boxes in a box subtree using a renderer. The viewport node
     * is always the first element in the resulting list.
     * @param vp the viewport to render
     */
    private List<Box> createBoxList(Viewport vp)
    {
        BoxListRenderer renderer = new BoxListRenderer(page.getIri(), zoom);
        renderer.init(vp);
        vp.draw(renderer);
        List<Box> ret = renderer.getBoxList();
        findIntrinsicParents(ret);
        return ret;
    }
    
    //===================================================================
    
    /**
     * Sets the intrinsicParent properties of all boxes in the list based on the same relationship
     * between their source CSSBox boxes.
     * @param boxes the list of boxes to process
     */
    private void findIntrinsicParents(List<Box> boxes)
    {
        Map<org.fit.cssbox.layout.Box, Box> srcBoxes = new HashMap<>();
        //index the boxes by their source CSSBox box
        for (Box box : boxes)
            srcBoxes.put(((BoxNode) box).getBox(), box);
        //map the parents
        for (Box box : boxes)
        {
            final BoxNode node = (BoxNode) box;
            if (node.getBox() != null && node.getBox().getParent() != null)
            {
                final Box parent = srcBoxes.get(node.getBox().getParent());
                if (parent != null)
                    node.setIntrinsicParent(parent);
            }
        }
    }
    
    private List<URL> loadList(String filename)
    {
        List<URL> ret = new ArrayList<URL>();
        try
        {
            BufferedReader read = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = read.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                {
                    ret.add(new URL(line));
                }
            }
            read.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
}
