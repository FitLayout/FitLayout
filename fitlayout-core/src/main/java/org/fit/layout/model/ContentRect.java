/**
 * ContentRect.java
 *
 * Created on 17. 10. 2014, 14:00:16 by burgetr
 */
package org.fit.layout.model;

/**
 * A generic rectangular content within a page
 * 
 * @author burgetr
 */
public interface ContentRect extends Rect
{

    /**
     * Obtains a unique ID of the area within the page.
     * @return the area ID
     */
    public int getId();
    
    /**
     * Obtains the page this block belongs to.
     * @return the page
     */
    public Page getPage();
    
    /**
     * Obtains the pixel position within in the page.
     * @return The rectangular pixel position. 
     */
    public Rectangular getBounds();
    
    /**
     * Obtains the background color of the area. 
     * @return A color or {@code null} for transparent background
     */
    public Color getBackgroundColor();

    /**
     * Obtains the amount of underlined text. 0 means no underlined text, 1 means all the text is underlined.
     * @return a value in the range 0..1
     */
    public float getUnderline();
    
    /**
     * Obtains the amount of line-through text. 0 means no underlined text, 1 means all the text is underlined.
     * @return a value in the range 0..1
     */
    public float getLineThrough();
    
    /**
     * Obtains an average font size in the are in pixels.
     * @return the average font pixel size
     */
    public float getFontSize();
    
    /**
     * Obtains the average font style. 0 means no text in italics, 1 means all the text in italics.
     * @return a value in the range 0..1
     */
    public float getFontStyle();
    
    /**
     * Obtains the average font style. 0 means no text is bold, 1 means all the text is bold
     * @return a value in the range 0..1
     */
    public float getFontWeight();
    
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

    
}
