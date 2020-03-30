/**
 * CSSBoxTreeBuilder.java
 *
 * Created on 24. 10. 2014, 23:52:25 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Engine;
import org.fit.cssbox.layout.GraphicsEngine;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.pdf.PdfEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Box.DisplayType;
import cz.vutbr.web.css.MediaSpec;

/**
 * This class implements building the box tree using the CSSBox rendering engine.
 * 
 * @author burgetr
 */
public class CSSBoxTreeBuilder
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
    
    /** Use real visual bounds instead of the element content bounds for building the box hierarchy */
    protected boolean useVisualBounds;
    
    protected boolean preserveAux;
    
    /** Replace the images with their {@code alt} text */
    protected boolean replaceImagesWithAlt;
    
    /** a counter for assigning the box order */
    private int order_counter;
    
    private float zoom;
    
   
    public CSSBoxTreeBuilder(Dimension pageSize, boolean useVisualBounds, boolean preserveAux, boolean replaceImagesWithAlt)
    {
        this.pageSize = pageSize;
        this.useVisualBounds = useVisualBounds;
        this.preserveAux = preserveAux;
        this.replaceImagesWithAlt = replaceImagesWithAlt;
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

    public void parse(URL url) throws IOException, SAXException
    {
        //render the page
        Engine engine = renderUrl(url, pageSize);
        viewport = engine.getViewport();
        PageImpl pg = page = new PageImpl(pageUrl);
        pg.setTitle(pageTitle);
        
        //construct the box tree
        ElementBox rootbox = engine.getViewport();
        BoxNode root = buildTree(rootbox);
        
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
        main.setPage(page);
        main.setTagName("pageset");
        
        for (URL url : list)
        {
            log.info("Parsing: {}", url);
            //render the page
            Engine engine = renderUrl(url, pageSize);
            ElementBox rootbox = engine.getViewport();
            BoxNode root = buildTree(rootbox);
            
            //wrap the page with a new block box
            DefaultBox pageBox = new DefaultBox();
            pageBox.setPage(page);
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
            engine.getConfig().setLoadImages(false);
            engine.getConfig().setLoadBackgroundImages(false);
            engine.getConfig().setLoadFonts(false);
            engine.getConfig().setReplaceImagesWithAlt(replaceImagesWithAlt);
            defineLogicalFonts(engine.getConfig());
            engine.createLayout(pageSize);
            
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
    
    protected BoxNode buildTree(ElementBox rootbox)
    {
        //create the working list of nodes
        log.trace("LIST");
        List<BoxNode> boxlist = new ArrayList<BoxNode>();
        order_counter = 1;
        createBoxList(rootbox, boxlist);
        
        //create the tree
        if (useVisualBounds)
        {
            //two-phase algorithm considering the visual bounds
            log.trace("A1");
            BoxNode root = createBoxTree(rootbox, boxlist, true, true, true); //create a nesting tree based on the content bounds
            log.trace("A2");
            Color bg = Units.toColor(rootbox.getBgcolor());
            if (bg == null) bg = Color.WHITE;
            computeBackgrounds(root, bg); //compute the efficient background colors
            log.trace("A2.5");
            root.recomputeVisualBounds(); //compute the visual bounds for the whole tree
            log.trace("A3");
            root = createBoxTree(rootbox, boxlist, true, true, preserveAux); //create the nesting tree based on the visual bounds or content bounds depending on the settings
            root.recomputeVisualBounds(); //compute the visual bounds for the whole tree
            root.recomputeBounds(); //compute the real bounds of each node
            log.trace("A4");
            //root.applyTransforms(); //TODO test this first; actually the transform should be applied according to the drawing tree, not this tree
            return root;
        }
        else
        {
            //simplified algorihm - use the original box nesting
            BoxNode root = createBoxTree(rootbox, boxlist, false, true, true);
            Color bg = Units.toColor(rootbox.getBgcolor());
            if (bg == null) bg = Color.WHITE;
            computeBackgrounds(root, bg); //compute the efficient background colors
            root.recomputeVisualBounds(); //compute the visual bounds for the whole tree
            root.recomputeBounds(); //compute the real bounds of each node
            root.applyTransforms();
            return root;
        }
    }
    
    /**
     * Recursively creates a list of all the visible boxes in a box subtree. The nodes are 
     * added to the end of a specified list. The previous content of the list 
     * remains unchanged. The 'viewport' box is ignored.
     * @param root the source root box
     * @param list the list that will be filled with the nodes
     */
    private void createBoxList(Box root, List<BoxNode> list)
    {
        if (root.isDisplayed())
        {
            if (!(root instanceof Viewport) && root.isVisible())
            {
                BoxNode newnode = new BoxNode(root, page, zoom);
                newnode.setOrder(order_counter++);
                list.add(newnode);
            }
            if (root instanceof ElementBox)
            {
                ElementBox elem = (ElementBox) root;
                for (int i = elem.getStartChild(); i < elem.getEndChild(); i++)
                    createBoxList(elem.getSubBox(i), list);
            }
        }
    }

    /**
     * Creates a tree of box nesting based on the content bounds of the boxes.
     * This tree is only used for determining the backgrounds.
     * 
     * @param boxlist the list of boxes to build the tree from
     * @param useBounds when set to {@code true}, the full or visual bounds are used for constructing the tree
     * depending on the {@code useVisualBounds} parameter. Otherwise, the original box hierarchy is used.
     * @param useVisualBounds when set to {@code true} the visual bounds are used for constructing the tree. Otherwise,
     * the content bounds are used. 
     * @param preserveAux when set to {@code true}, all boxes are preserved. Otherwise, only the visually
     * distinguished ones are preserved.
     */
    private BoxNode createBoxTree(ElementBox rootbox, List<BoxNode> boxlist, boolean useBounds, boolean useVisualBounds, boolean preserveAux)
    {
        //a working copy of the box list
        List<BoxNode> list = new ArrayList<BoxNode>(boxlist);

        //an artificial root node
        BoxNode root = new BoxNode(rootbox, page, zoom);
        root.setOrder(0);
        //detach the nodes from any old trees
        for (BoxNode node : list)
            node.removeFromTree();
        
        //when working with visual bounds, remove the boxes that are not visually separated
        if (!preserveAux)
        {
            for (Iterator<BoxNode> it = list.iterator(); it.hasNext(); )
            {
                BoxNode node = it.next();
                if (!node.isVisuallySeparated() || !node.isVisible())
                    it.remove();
            }
        }
        
        //let each node choose it's children - find the roots and parents
        for (BoxNode node : list)
        {
            if (useBounds)
                node.markNodesInside(list, useVisualBounds);
            else
                node.markChildNodes(list);
        }
        
        //choose the roots
        for (Iterator<BoxNode> it = list.iterator(); it.hasNext();)
        {
            BoxNode node = it.next();
            
            /*if (!full) //DEBUG
            {
               if (node.toString().contains("mediawiki") || node.toString().contains("globalWrapper"))
                    System.out.println(node + " => " + node.nearestParent);
            }*/
            
            if (node.isRootNode())
            {
                root.appendChild(node);
                it.remove();
            }
        }
        
        //recursively choose the children
        for (int i = 0; i < root.getChildCount(); i++)
            ((BoxNode) root.getChildAt(i)).takeChildren(list);
        
        return root;
    }
    
    /**
     * Computes efficient background color for all the nodes in the tree
     */
    private void computeBackgrounds(BoxNode root, Color currentbg)
    {
        Color newbg = root.getBackgroundColor();
        if (newbg == null)
            newbg = currentbg;
        root.setEfficientBackground(newbg);
        root.setBackgroundSeparated(!newbg.equals(currentbg));
        
        for (int i = 0; i < root.getChildCount(); i++)
            computeBackgrounds((BoxNode) root.getChildAt(i), newbg);
    }
    
    //===================================================================
    
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
