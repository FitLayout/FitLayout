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
import cz.vutbr.fit.layout.model.Rectangular;

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
    
    private int nextPageY = 0; // the Y coordinate of the next page
    
    

    public FLBoxTree() throws IOException
    {
        super();
        allBoxes = new ArrayList<>();
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
        // TODO root box properties
        allBoxes.add(root);
    }

    @Override
    protected void endDocument(PDDocument document) throws IOException
    {
    }

    @Override
    protected void startNewPage()
    {
        PDRectangle layout = getCurrentMediaBox();
        if (layout != null)
        {
            float w = layout.getWidth();
            float h = layout.getHeight();
            final int rot = pdpage.getRotation();
            if (rot == 90 || rot == 270)
            {
                float x = w; w = h; h = x;
            }
            
            pageBox = createBox(0, nextPageY, convertLength(w), convertLength(h));
            nextPageY += convertLength(h) + PAGE_GAP;
            
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
                convertLength(curstyle.getTop()),
                convertLength(metrics.getWidth()),
                convertLength(metrics.getHeight()));
        textBox.setType(Type.TEXT_CONTENT);
        textBox.setOwnText(data);
        //TODO style
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
        ret.setBounds(new Rectangular(x, y, x + w - 1, y + h - 1, false));
        return ret;
    }
    
    protected int convertLength(float length)
    {
        return Math.round(length); //TODO convert pt to px?
    }

}
