/**
 * Page.java
 *
 * Created on 17. 10. 2014, 14:02:05 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.net.URL;
import java.util.Vector;

/**
 * This class represents the whole rendered (and segmented) page.
 * 
 * @author burgetr
 */
public interface Page
{

    /**
     * Obtains the source URL of the page.
     * @return the URL
     */
    public URL getSourceURL();
    
    /**
     * Obtains the page title specified using the {@code <title>} tag.
     * @return the page title or an empty string when not set
     */
    public String getTitle();
    
    /**
     * Obtains the page width.
     * @return the width in pixels
     */
    public int getWidth();

    /**
     * Obtains the page height.
     * @return the height in pixels
     */
    public int getHeight();

    /**
     * Returns the root box of the page.
     * @return the root box
     */
    public Box getRoot();
    
    /**
     * Finds the deepest node in the tree that contains the given point.
     * @param x The point X coordinate
     * @param y The point Y coordinate
     * @return The deepest box at the given coordinates or {@code null} when there is no box
     * in the tree placed at the given coordinates.
     */
    public Box getBoxAt(int x, int y);
    
    /**
     * Finds the root boxes of subtrees that are fully contained in the given region based on their visual bounds.
     * @param r The region to be tested
     * @return A vector of subtree root boxes (possibly empty)
     */
    public Vector<Box> getBoxesInRegion(Rectangular r);
    
    //TODO some factory functions (create area, etc?)
}
