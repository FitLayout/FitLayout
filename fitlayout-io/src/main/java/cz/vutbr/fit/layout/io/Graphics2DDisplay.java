/**
 * OutputDisplayImpl.java
 *
 * Created on 31. 10. 2014, 13:47:46 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.ContentImage;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;

/**
 * An output display implementation that shows the areas on a Graphics2D device.
 * 
 * @author burgetr
 */
public class Graphics2DDisplay implements OutputDisplay
{
    private Graphics2D g;
    private Color boxLogicalColor = Color.RED;
    private Color boxContentColor = Color.GREEN;
    private Color areaBoundsColor = Color.MAGENTA;
    
    public Graphics2DDisplay(Graphics2D g)
    {
        this.g = g;
    }

    @Override
    public Graphics2D getGraphics()
    {
        return g;
    }

    public void setGraphics(Graphics2D g)
    {
        this.g = g;
    }
    
    @Override
    public void drawPage(Page page)
    {
        setupGraphics();
        recursivelyDrawBoxes(page.getRoot());
    }

    @Override
    public void drawPage(Page page, boolean bitmap)
    {
        setupGraphics();
        if (bitmap)
            drawScreenShot(page.getPngImage());
        else
            recursivelyDrawBoxes(page.getRoot());
    }

    private void recursivelyDrawBoxes(Box root)
    {
        drawBox(root);
        for (int i = 0; i < root.getChildCount(); i++)
            recursivelyDrawBoxes(root.getChildAt(i));
    }
    
    @Override
    public void drawBox(Box box)
    {
        Box.Type type = box.getType();
        
        if (type == Box.Type.TEXT_CONTENT)
        {
            g.setColor(toAWTColor(box.getColor()));
            
            //setup the font
            String fmlspec = box.getFontFamily();
            float fontsize = box.getTextStyle().getFontSize(); //AWT font assumes 72dpi, i.e. the required point size is our pixel size
            int fs = Font.PLAIN;
            if (box.getTextStyle().getFontWeight() > 0.5f)
                fs = Font.BOLD;
            if (box.getTextStyle().getFontStyle() > 0.5f)
                fs = fs | Font.ITALIC;
            
            //Create base font
            Font font = new Font(fmlspec, fs, (int) fontsize);
            //Use kerning
            Map<TextAttribute, Object> attributes = new HashMap<>();
                attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            font = font.deriveFont(attributes);
            g.setFont(font);
            
            String text = box.getText();
            if (text.length() > 0)
            {
                AttributedString as = new AttributedString(text);
                as.addAttribute(TextAttribute.FONT, font);
                if (box.getTextStyle().getUnderline() > 0.5f)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                if (box.getTextStyle().getLineThrough() > 0.5f)
                    as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                
                FontMetrics fm = g.getFontMetrics();
                Rectangle2D rect = fm.getStringBounds(text, g);
                int x = box.getX1() + (int) rect.getX();
                int y = box.getY1() - (int) rect.getY();
                g.drawString(as.getIterator(), x, y);
            }
        }
        else if (type == Box.Type.REPLACED_CONTENT)
        {
            final ContentObject obj = box.getContentObject();
            if (obj != null && obj instanceof ContentImage && ((ContentImage) obj).getPngData() != null)
            {
                try
                {
                    byte[] pngData = ((ContentImage) obj).getPngData();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngData));
                    g.drawImage(image, box.getBounds().getX1(), box.getBounds().getY1(), null);
                } catch (IOException e) {
                }
            }
            else
            {
                g.setColor(toAWTColor(box.getColor()));
                Rectangular r = box.getBounds();
                g.drawRect(r.getX1(), r.getY1(), r.getWidth() - 1, r.getHeight() - 1);
            }
        }
        else //element boxes
        {
            Rectangular r = box.getBounds();
            //background color
            Color bg = toAWTColor(box.getBackgroundColor());
            if (bg != null)
            {
                g.setColor(bg);
                g.fillRect(r.getX1(), r.getY1(), r.getWidth() - 1, r.getHeight() - 1);
            }
            //background image
            if (box.getBackgroundImagePng() != null)
            {
                try {
                    final BufferedImage image = ImageIO.read(new ByteArrayInputStream(box.getBackgroundImagePng()));
                    g.drawImage(image, r.getX1(), r.getY1(), null);
                } catch (IOException e) {
                }
            }
            //borders
            Stroke oldStroke = g.getStroke();
            if (box.hasTopBorder())
            {
                final Border bst = box.getBorderStyle(Border.Side.TOP);
                drawBorder(g, r.getX1(), r.getY1(), r.getX2(), r.getY1(), bst.getWidth(), 0, 0, bst, false);
            }
            if (box.hasRightBorder())
            {
                final Border bst = box.getBorderStyle(Border.Side.RIGHT);
                drawBorder(g, r.getX2(), r.getY1(), r.getX2(), r.getY2(), bst.getWidth(), -bst.getWidth() + 1, 0, bst, true);
            }
            if (box.hasBottomBorder())
            {
                final Border bst = box.getBorderStyle(Border.Side.BOTTOM);
                drawBorder(g, r.getX1(), r.getY2(), r.getX2(), r.getY2(), bst.getWidth(), 0, -bst.getWidth() + 1, bst, true);
            }
            if (box.hasLeftBorder())
            {
                final Border bst = box.getBorderStyle(Border.Side.LEFT);
                drawBorder(g, r.getX1(), r.getY1(), r.getX1(), r.getY2(), bst.getWidth(), 0, 0, bst, false);
            }
            g.setStroke(oldStroke);
        }

    }
    
    private void drawBorder(Graphics2D g, int x1, int y1, int x2, int y2,
            int width, int right, int down, Border style, boolean reverse)
    {
        if (style.getWidth() >= 1)
        {
            g.setColor(toAWTColor(style.getColor()));
            g.setStroke(new BorderStroke(width, style.getStyle(), reverse));
            g.draw(new Line2D.Double(x1 + right, y1 + down, x2 + right, y2 + down));
        }
    }

    private void drawScreenShot(byte[] data)
    {
        if (data != null)
        {
            try {
                final BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
                g.drawImage(image, 0, 0, null);
            } catch (IOException e) {
            }
        }
    }
    
    //=================================================================================

    @Override
    public void drawExtent(Box box)
    {
        //draw the visual content box
        g.setColor(boxLogicalColor);
        Rectangular r = box.getBounds();
        g.drawRect(r.getX1(), r.getY1(), r.getWidth() - 1, r.getHeight() - 1);
        
        //draw the visual content box
        g.setColor(boxContentColor);
        r = box.getVisualBounds();
        g.drawRect(r.getX1(), r.getY1(), r.getWidth() - 1, r.getHeight() - 1);
    }
    
    @Override
    public void drawExtent(Area area)
    {
        Rectangular bounds = area.getBounds();
        Color c = g.getColor();
        g.setColor(areaBoundsColor);
        g.drawRect(bounds.getX1(), bounds.getY1(), bounds.getWidth() - 1, bounds.getHeight() - 1);
        g.setColor(c);
    }

    @Override
    public void drawRectangle(Rectangular rect, Color color)
    {
        Color c = g.getColor();
        g.setColor(color);
        g.fillRect(rect.getX1(), rect.getY1(), rect.getWidth(), rect.getHeight());
        g.setColor(c);
    }
    
    @Override
    public void drawConnection(ContentRect a1, ContentRect a2, Color color)
    {
        Color c = g.getColor();
        g.setColor(color);
        g.drawLine(a1.getBounds().midX(), a1.getBounds().midY(),
                a2.getBounds().midX(), a2.getBounds().midY());
        g.setColor(c);
    }

    @Override
    public void colorizeByTags(ContentRect rect, Set<Tag> s)
    {
        if (!s.isEmpty())
        {
            Rectangular bounds = rect.getBounds();
            Color c = g.getColor();
            float step = (float) bounds.getHeight() / s.size();
            float y = bounds.getY1();
            for (Iterator<Tag> it = s.iterator(); it.hasNext();)
            {
                Tag tag = it.next();
                g.setColor(stringColor(tag.getValue()));
                g.fillRect(bounds.getX1(), (int) y, bounds.getWidth(), (int) (step+0.5));
                y += step;
            }
            g.setColor(c);
        }
    }

    @Override
    public void clearArea(int x, int y, int width, int height)
    {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(x, y, width, height);
        g.setComposite(AlphaComposite.SrcOver);
    }
    
    @Override
    public void colorizeByClass(ContentRect rect, String cname)
    {
        if (cname != null && !cname.equals("") && !cname.equals("none"))
        {
            Rectangular bounds = rect.getBounds();
            Color c = g.getColor();
            float step = (float) bounds.getHeight();
            float y = bounds.getY1();
            g.setColor(stringColor(cname));
            g.fillRect(bounds.getX1(), (int) y, bounds.getWidth(), (int) (step+0.5));
            g.setColor(c);
        }
    }

    /**
     * Configures the graphics context for drawing the boxes. This method is normally called
     * from {@link #drawPage(Page)} before the actual drawing starts.
     */
    protected void setupGraphics()
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
    
    protected Color stringColor(String cname)                                 
    {                                                                            
            if (cname == null || cname.equals(""))       
                    return Color.WHITE;                                                 
                                                                                 
            String s = new String(cname);                                        
            while (s.length() < 6) s = s + s;                                    
            int r = (int) s.charAt(0) *  (int) s.charAt(1);                      
            int g = (int) s.charAt(2) *  (int) s.charAt(3);                      
            int b = (int) s.charAt(4) *  (int) s.charAt(5);                      
            Color ret = new Color(100 + (r % 150), 100 + (g % 150), 100 + (b % 150), 128);              
            //System.out.println(cname + " => " + ret.toString());               
            return ret;                                                          
    }
    
    private Color toAWTColor(cz.vutbr.fit.layout.model.Color src)
    {
        if (src == null)
            return null;
        else
            return new Color(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
    }
    
}
