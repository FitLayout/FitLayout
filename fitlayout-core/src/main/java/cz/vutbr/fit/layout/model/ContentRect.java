/**
 * ContentRect.java
 *
 * Created on 17. 10. 2014, 14:00:16 by burgetr
 */
package cz.vutbr.fit.layout.model;

import org.eclipse.rdf4j.model.IRI;

/**
 * A generic rectangular content within a page.
 * 
 * @author burgetr
 */
public interface ContentRect extends Rect
{
    /** A node attribute that links to an equivalent node (e.g. produced by copying
     * a node to another tree). */
    public static String ATTR_SAME_AS = "core.node.sameAs";

    /**
     * Obtains a unique ID of the area within the page.
     * @return the area ID
     */
    public int getId();
    
    /**
     * Obtains the IRI of the page this block belongs to.
     * @return the page IRI
     */
    public IRI getPageIri();
    
    /**
     * Obtains the pixel position within in the page.
     * @return The rectangular pixel position. 
     */
    public Rectangular getBounds();
    
    /**
     * Sets the pixel position within the page.
     * @param bounds the new position
     */
    public void setBounds(Rectangular bounds);
    
    /**
     * Obtains the background color of the area. 
     * @return A color or {@code null} for transparent background
     */
    public Color getBackgroundColor();

    /**
     * Gets the PNG image data of the background image if present. The image should
     * have the same size as the content bounds. 
     * @return the image data or {@code null} if no image is present in the background
     */
    public byte[] getBackgroundImagePng();

    /**
     * Checks whether the box has a background color or image.
     * @return {@code true} when the box has a defined background color or image
     */
    public boolean hasBackground();

    /**
     * Gets the text style statistics of the content.
     * @return the text style statistics
     */
    public TextStyle getTextStyle();
    
    /**
     * Returns the content line the area belongs to.
     * @return The content line or {@code null} when the area does not belong to any line
     */
    public ContentLine getLine();
    
    /**
     * Assigns the content line to the area.
     * @param line the content line to be assigned.
     */
    public void setLine(ContentLine line);
    
    //=================================================================================================
    // Borders
    //=================================================================================================
    
    /**
     * Obtains the number of defined borders for the box.
     * @return the number of defined borders (0..4)
     */
    public int getBorderCount();
    
    /**
     * Checks whether the box has the top border defined. 
     * @return <code>true</code> if the box has a top border
     */
    public boolean hasTopBorder();
    
    /**
     * Obtains the top border width.
     * @return the width of the border or 0 when there is no border
     */
    public int getTopBorder();

    /**
     * Checks whether the box has the bottom border defined. 
     * @return <code>true</code> if the box has a bottom border
     */
    public boolean hasBottomBorder();
    
    /**
     * Obtains the bottom border width.
     * @return the width of the border or 0 when there is no border
     */
    public int getBottomBorder();

    /**
     * Checks whether the box has the left border defined. 
     * @return <code>true</code> if the box has a left border
     */
    public boolean hasLeftBorder();
    
    /**
     * Obtains the left border width.
     * @return the width of the border or 0 when there is no border
     */
    public int getLeftBorder();

    /**
     * Checks whether the box has right top border defined. 
     * @return <code>true</code> if the box has a right border
     */
    public boolean hasRightBorder();
    
    /**
     * Obtains the right border width.
     * @return the width of the border or 0 when there is no border
     */
    public int getRightBorder();
    
    /**
     * Obtains the properties of the box border at the given side.
     * @param side the border side.
     * @return the corresponding border properties.
     */
    public Border getBorderStyle(Border.Side side);
    
    
    //=================================================================================================
    // Background
    //=================================================================================================
    
    /**
     * Checks whether the box is separated by background, i.e. its background color is not transparent
     * and it is different from the ancestor background.
     * @return {@code true} for background-separated boxes
     */
    public boolean isBackgroundSeparated();
    
    /**
     * Sets whether the box is separated by background. This is typically determined and set during the
     * box tree construction.
     * @param backgroundSeparated
     */
    public void setBackgroundSeparated(boolean backgroundSeparated);

    //=================================================================================================
    // User attributes
    //=================================================================================================
    
    /**
     * Sets a user-defined attribute for the tree node. This allows to assign multiple
     * attributes identified by their names.
     * @param name the attribute name
     * @param value the attribute value
     */
    public void addUserAttribute(String name, Object value);
    
    /**
     * Obtains the user-defined attribute value assigned to the node.
     * @param name the attribute name
     * @param clazz the class of the required attribute
     * @return an object of the given class representing the value of the attribute (application-specific)
     * or {@code null} when no such attribute is present.
     */
    public <P> P getUserAttribute(String name, Class<P> clazz);


}
