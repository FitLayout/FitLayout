/**
 * Page.java
 *
 * Created on 17. 10. 2014, 14:02:05 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the whole rendered (and segmented) page.
 * 
 * @author burgetr
 */
public interface Page extends Artifact
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
     * Gets the PNG image of the rendered page if provided by the renderer.
     * @return PNG image data array or {@code null} when the page image was not provided by the renderer.
     */
    public byte[] getPngImage();
    
    /**
     * Metadata provided at page level. The format depends on the metadata source.
     * For JSON-LD metadata this is a generic JSON object.
     * @return The metadata object or {@code null} when no metadata is provided.
     */
    public Collection<Metadata> getMetadata();
    
    /**
     * Finds all the boxes that contain the given point within their visual bounds.
     * @param x The point X coordinate
     * @param y The point Y coordinate
     * @return A list of boxes that contain [x, y] within their visual bounds.
     */
    public List<Box> getBoxesAt(int x, int y);
    
    /**
     * Finds the root boxes of subtrees that are fully contained in the given region based on their visual bounds.
     * @param r The region to be tested
     * @return A list of subtree root boxes (possibly empty)
     */
    public List<Box> getBoxesInRegion(Rectangular r);
    
    /**
     * Creates a new empty box wihtin this page.
     * @return the new box
     */
    public Box createBox();
    
    /**
     * Creates a new box within this page by copying an existing one.
     * @param src the source box to copy
     * @return the new box
     */
    public Box createBox(Box src);
    
}
