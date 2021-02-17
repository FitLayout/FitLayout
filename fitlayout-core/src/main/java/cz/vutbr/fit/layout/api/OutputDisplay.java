/**
 * OutputDisplay.java
 *
 * Created on 31. 10. 2014, 10:28:25 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Set;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;

/**
 * An abstraction of the graphical display of the segmentation output. It allows drawing
 * the area bounds and distinguishing the area types by colors.
 * 
 * @author burgetr
 */
public interface OutputDisplay
{
    
    public Graphics2D getGraphics();
    
    /**
     * Draws the complete page including all the boxes.
     * @param page The page to draw.
     */
    public void drawPage(Page page);

    /**
     * Draws the complete page by drawing all the boxes or a bitmap screenshot.
     * @param page The page to draw.
     * @param bitmap Use bitmap page screen shot if available.
     */
    public void drawPage(Page page, boolean bitmap);

    /**
     * Draws the box contents on the page depending on the box type. This does not automatically
     * draw the child boxes.
     * @param box The box do draw.
     */
    public void drawBox(Box box);
    
    /**
     * Draws the box bounds at the output display.
     * @param box The box to be displayed
     */
    public void drawExtent(Box box);
    
    /**
     * Draws the area bounds at the output display.
     * @param area The area to be displayed
     */
    public void drawExtent(Area area);
    
    /**
     * Draws the given rectangle with the specified color.
     * @param rect the rectangle to be drawn
     * @param color the drawing color
     */
    public void drawRectangle(Rectangular rect, Color color);
    
    /**
     * Draws the colorized are bounds. The color is defined by the tags. Multiple colors should
     * be used when there are multiple tags (e.g. splitting the area bounds to several parts).
     * The output display should assign different colors to different tags; the exact implementation
     * of the color mapping depends on the OutputDisplay implementation. The area is not displayed
     * when the tag set is empty.
     * @param area The area to be displayed
     * @param s A set of tags used for generating the area colors
     */
    public void colorizeByTags(Area area, Set<Tag> s);
    
    /**
     * Draws the colorized are bounds. The color is defined by a string. The output display
     * should assign different colors to different strings; the exact implementation
     * of the color mapping depends on the OutputDisplay implementation. The area is not displayed
     * when class name is empty.
     * @param area  The area to be displayed
     * @param cname The class name used for generating the colors.
     */
    public void colorizeByClass(Area area, String cname);

    /**
     * Clears the given area of the display.
     * @param x the area X coordinate
     * @param y the area Y coordinate
     * @param width the area width
     * @param height the area height
     */
    public void clearArea(int x, int y, int width, int height);
    
}
