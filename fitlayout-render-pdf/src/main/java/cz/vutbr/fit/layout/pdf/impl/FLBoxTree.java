/**
 * PDFBoxTree.java
 *
 * Created on 12. 10. 2022, 14:56:34 by burgetr
 */
package cz.vutbr.fit.layout.pdf.impl;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.fit.pdfdom.HtmlDivLine;
import org.fit.pdfdom.PDFBoxTree;
import org.fit.pdfdom.PathSegment;
import org.fit.pdfdom.TextMetrics;
import org.fit.pdfdom.resource.ImageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultContentImage;
import cz.vutbr.fit.layout.model.Border;
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
    
    private static final Color DOCUMENT_COLOR = new Color(0xee, 0xee, 0xee);
    private static final Color PAGE_COLOR = Color.WHITE;
    private static final int PAGE_GAP = 10;
    
    private boolean acquireImages = true;
    private float zoom = 1.0f;
    
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
    
    public boolean isAcquireImages()
    {
        return acquireImages;
    }

    public void setAcquireImages(boolean acquireImages)
    {
        this.acquireImages = acquireImages;
    }

    public float getZoom()
    {
        return zoom;
    }

    public void setZoom(float zoom)
    {
        this.zoom = zoom;
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
    
    protected void addBox(BoxImpl parent, BoxImpl newBox)
    {
        allBoxes.add(newBox);
        if (parent != null)
        {
            parent.appendChild(newBox);
            newBox.setIntrinsicParent(parent);
        }
    }

    @Override
    protected void startDocument(PDDocument document) throws IOException
    {
        root = createBox(0, 0, 100, 100);
        root.setTagName("#document");
        root.setBackgroundColor(DOCUMENT_COLOR);
        addBox(null, root);
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
            int w = convertLengthI(layout.getWidth());
            int h = convertLengthI(layout.getHeight());
            final int rot = pdpage.getRotation();
            if (rot == 90 || rot == 270)
            {
                int x = w; w = h; h = x;
            }
            
            curPageY = nextPageY;
            pageBox = createBox(0, curPageY, w, h);
            pageBox.setTagName("page");
            pageBox.setBackgroundColor(PAGE_COLOR);
            
            nextPageY += h + PAGE_GAP;
            maxPageWidth = Math.max(maxPageWidth, w);
            
            addBox(root, pageBox);
        }
        else
            log.warn("No media box found");
    }

    @Override
    protected void renderText(String data, TextMetrics metrics)
    {
        final BoxImpl textBox = createBox(convertLengthI(curstyle.getLeft()),
                curPageY + convertLengthI(curstyle.getTop()),
                convertLengthI(metrics.getWidth()),
                convertLengthI(metrics.getHeight()));
        textBox.setType(Type.TEXT_CONTENT);
        textBox.setOwnText(data);
        textBox.setFontFamily(curstyle.getFontFamily());
        textBox.setTextStyle(getCurrentTextStyle(data.length()));
        textBox.setColor(parseColor(curstyle.getColor()));
        addBox(pageBox, textBox);
    }

    @Override
    protected void renderPath(List<PathSegment> path, boolean stroke, boolean fill) throws IOException
    {
        float[] rect = toRectangle(path);
        if (rect != null)
        {
            final BoxImpl rectBox = createRectangleBox(rect[0], rect[1], rect[2]-rect[0], rect[3]-rect[1], curPageY, stroke, fill);
            addBox(pageBox, rectBox);
        }
        else if (stroke)
        {
            for (PathSegment segm : path)
            {
                BoxImpl lineBox = createLineBox(segm.getX1(), segm.getY1(), segm.getX2(), segm.getY2(), curPageY);
                addBox(pageBox, lineBox);
            }
        }
    }

    @Override
    protected void renderImage(float x, float y, float width, float height, ImageResource data) throws IOException
    {
        BoxImpl imageBox = createImageBox(x, y, width, height, curPageY, data);
        addBox(pageBox, imageBox);
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
        ret.setSourceNodeId("b" + ret.getOrder());
        ret.setBounds(new Rectangular(x, y, x + w - 1, y + h - 1, false));
        ret.setContentBounds(new Rectangular(ret.getBounds()));
        return ret;
    }
    
    protected BoxImpl createRectangleBox(float x, float y, float width, float height, int pageOffset, boolean stroke, boolean fill)
    {
        float lineWidth = transformWidth(getGraphicsState().getLineWidth());
        float wcor = stroke ? lineWidth : 0.0f;
        float strokeOffset = wcor / 2;
        float w = width + wcor;
        float h = height + wcor;
        
        int dx = convertLengthI(x - strokeOffset);
        int dy = convertLengthI(y - strokeOffset) + pageOffset;
        int dw = convertLengthI(w);
        int dh = convertLengthI(h);
        
        BoxImpl ret = createBox(dx, dy, dw, dh);
        ret.setTagName("rect");
        ret.setType(Type.ELEMENT);
        
        if (stroke)
        {
            Color clr = convertColor(getGraphicsState().getStrokingColor());
            Border b = new Border(convertLengthI(lineWidth), Border.Style.SOLID, clr);
            for (Border.Side side : Border.Side.values())
                ret.setBorderStyle(side, b);
        }
        
        if (fill)
        {
            Color clr = convertColor(getGraphicsState().getNonStrokingColor());
            ret.setBackgroundColor(clr);
        }
        
        return ret;
    }
    
    protected BoxImpl createLineBox(float x1, float y1, float x2, float y2, int pageOffset)
    {
        HtmlDivLine line = new HtmlDivLine(x1, y1, x2, y2, transformWidth(getGraphicsState().getLineWidth()));
        Color color = convertColor(getGraphicsState().getStrokingColor());
        
        BoxImpl ret = createBox(convertLengthI(line.getLeft()),
                convertLengthI(line.getTop()) + pageOffset,
                convertLengthI(line.getWidth()),
                convertLengthI(line.getHeight()));
        ret.setTagName("line");
        ret.setType(Type.ELEMENT);
        
        Border.Side side = line.isVertical() ? Border.Side.RIGHT : Border.Side.BOTTOM;
        ret.setBorderStyle(side, new Border(convertLengthI(line.getLineStrokeWidth()), Border.Style.SOLID, color));
        
        return ret;
    }
    
    protected BoxImpl createImageBox(float x, float y, float width, float height, int pageOffset, ImageResource resource) throws IOException
    {
        BoxImpl ret = createBox(convertLengthI(x),
                convertLengthI(y) + pageOffset,
                convertLengthI(width),
                convertLengthI(height));
        
        ret.setTagName("img");
        ret.setType(Type.REPLACED_CONTENT);
        //ret.setBackgroundColor(Color.BLACK);
        
        if (isAcquireImages() && resource.getData() != null)
        {
            try {
                DefaultContentImage img = new DefaultContentImage();
                byte[] scaled = scaleImageData(resource.getData(), convertLengthI(width), convertLengthI(height));
                img.setPngData(scaled);
                ret.setContentObject(img);
            } catch (IOException e) {
            }
        }
        
        return ret;
    }
    
    protected float convertLength(float length)
    {
        return length * (96.0f / 72.0f) * zoom; // convert from pt to pixels (considering 96 dpi) and zoom
    }
    
    protected int convertLengthI(float length)
    {
        int ret = Math.round(convertLength(length)); 
        if (ret == 0 && length > 0.1f) // convert minimal widths to at least 1px
            ret = 1;
        return ret;
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
    
    protected Color convertColor(PDColor pdcolor)
    {
        Color color = null;
        try
        {
            float[] rgb = pdcolor.getColorSpace().toRGB(pdcolor.getComponents());
            color = new Color(Math.round(rgb[0] * 255), Math.round(rgb[1] * 255), Math.round(rgb[2] * 255));
        } catch (IOException e) {
            log.error("convertColor: IOException: {}", e.getMessage());
        } catch (UnsupportedOperationException e) {
            log.error("convertColor: UnsupportedOperationException: {}", e.getMessage());
        }
        return color;
    }

    public byte[] scaleImageData(byte[] srcData, int destWidth, int destHeight) throws IOException
    {
        ByteArrayInputStream is = new ByteArrayInputStream(srcData);
        BufferedImage srcImage = ImageIO.read(is);
        BufferedImage scaledImage = scaleImage(srcImage, destWidth, destHeight);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, "png", os);
        return os.toByteArray();
    }
    
    public static BufferedImage scaleImage(BufferedImage source, int destWidth, int destHeight)
    {
        BufferedImage copy = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = copy.getGraphics();
        gfx.drawImage(source, 0, 0, destWidth, destHeight, null);
        gfx.dispose();
        return copy;
    }

}
