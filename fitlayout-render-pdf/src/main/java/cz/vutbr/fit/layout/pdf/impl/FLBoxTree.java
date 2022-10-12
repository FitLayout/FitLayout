/**
 * PDFBoxTree.java
 *
 * Created on 12. 10. 2022, 14:56:34 by burgetr
 */
package cz.vutbr.fit.layout.pdf.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.fit.pdfdom.PDFBoxTree;
import org.fit.pdfdom.PathSegment;
import org.fit.pdfdom.TextMetrics;
import org.fit.pdfdom.resource.ImageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;

/**
 * 
 * @author burgetr
 */
public class FLBoxTree extends PDFBoxTree
{
    private static Logger log = LoggerFactory.getLogger(FLBoxTree.class);
    
    private static final int PAGE_GAP = 10;
    
    private List<Box> allBoxes;
    private BoxImpl root;
    private BoxImpl pageBox;
    
    private int curPageY = 0;
    private int nextPageY = 0; // the Y coordinate of the next page
    private int maxPageWidth = 0;
    private int orderCounter = 0;
    
    

    public FLBoxTree() throws IOException
    {
        super();
        allBoxes = new ArrayList<>();
    }
    
    public List<Box> getAllBoxes()
    {
        return allBoxes;
    }

    public BoxImpl getRoot()
    {
        return root;
    }

    public void processDocument(PDDocument pdfdocument, int startPage, int endPage) throws IOException
    {
        setStartPage(startPage);
        setEndPage(endPage);
        processPDF(pdfdocument);
    }

    protected void processPDF(PDDocument doc) throws IOException
    {
        /* We call the original PDFTextStripper.writeText but nothing should
           be printed actually because our processing methods produce no output.
           They create the DOM structures instead */
        super.writeText(doc, new OutputStreamWriter(System.out));
    }

    @Override
    protected void startDocument(PDDocument document) throws IOException
    {
        root = createBox(0, 0, 100, 100);
        root.setTagName("#document");
        root.setBackgroundColor(new Color(0xee, 0xee, 0xee));
        // TODO root box properties
        allBoxes.add(root);
    }

    @Override
    protected void endDocument(PDDocument document) throws IOException
    {
        root.setBounds(new Rectangular(0, 0, maxPageWidth - 1, nextPageY - 1));
        root.setContentBounds(new Rectangular(root.getBounds()));
    }

    @Override
    protected void startNewPage()
    {
        PDRectangle layout = getCurrentMediaBox();
        if (layout != null)
        {
            int w = convertLength(layout.getWidth());
            int h = convertLength(layout.getHeight());
            final int rot = pdpage.getRotation();
            if (rot == 90 || rot == 270)
            {
                int x = w; w = h; h = x;
            }
            
            curPageY = nextPageY;
            pageBox = createBox(0, curPageY, w, h);
            pageBox.setTagName("page");
            pageBox.setBackgroundColor(Color.WHITE);
            
            nextPageY += h + PAGE_GAP;
            maxPageWidth = Math.max(maxPageWidth, w);
            
            root.appendChild(pageBox);
            allBoxes.add(pageBox);
        }
        else
            log.warn("No media box found");
    }

    @Override
    protected void renderText(String data, TextMetrics metrics)
    {
        final BoxImpl textBox = createBox(convertLength(curstyle.getLeft()),
                curPageY + convertLength(curstyle.getTop()),
                convertLength(metrics.getWidth()),
                convertLength(metrics.getHeight()));
        textBox.setType(Type.TEXT_CONTENT);
        textBox.setOwnText(data);
        textBox.setFontFamily(curstyle.getFontFamily());
        textBox.setTextStyle(getCurrentTextStyle(data.length()));
        textBox.setColor(parseColor(curstyle.getColor()));
        pageBox.appendChild(textBox);
        allBoxes.add(textBox);
    }

    @Override
    protected void renderPath(List<PathSegment> path, boolean stroke,
            boolean fill) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void renderImage(float x, float y, float width, float height,
            ImageResource data) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
    
    //=============================================================================
 
    protected BoxImpl createBox()
    {
        final BoxImpl ret = new BoxImpl();
        return ret;
    }
    
    protected BoxImpl createBox(int x, int y, int w, int h)
    {
        final BoxImpl ret = createBox();
        ret.setOrder(orderCounter++);
        ret.setId(ret.getOrder());
        ret.setBounds(new Rectangular(x, y, x + w - 1, y + h - 1, false));
        ret.setContentBounds(new Rectangular(ret.getBounds()));
        return ret;
    }
    
    protected int convertLength(float length)
    {
        return Math.round(length * 1.5f); //TODO convert pt to px?
    }
    
    protected TextStyle getCurrentTextStyle(int contentLength)
    {
        final TextStyle ret = new TextStyle();
        ret.setContentLength(contentLength);
        ret.setFontSizeSum(convertLength(curstyle.getFontSize()) * contentLength);
        ret.setFontWeightSum(styleValue(curstyle.getFontWeight(), "bold") * contentLength);
        ret.setFontStyleSum(styleValue(curstyle.getFontStyle(), "italic") * contentLength);
        return ret;
    }
    
    protected int styleValue(String val, String oneval)
    {
        return oneval.equals(val) ? 1 : 0;
    }
    
    protected Color parseColor(String hash)
    {
        // color written in #ABC format
        if (hash.length() == 4) 
        {
            final String r = hash.substring(1, 2);
            final String g = hash.substring(2, 3);
            final String b = hash.substring(3, 4);
            return new Color(Integer.parseInt(r+r, 16), Integer.parseInt(g+g, 16), Integer.parseInt(b+b, 16));
        }
        // color written in #AABBCC format
        else if (hash.length() == 7) {
            final String r = hash.substring(1, 3);
            final String g = hash.substring(3, 5);
            final String b = hash.substring(5, 7);
            return new Color(Integer.parseInt(r, 16), Integer.parseInt(g, 16), Integer.parseInt(b, 16));
        }
        // invalid hash
        return Color.BLACK;
    }
    

}
