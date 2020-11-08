/**
 * Box.java
 *
 * Created on 17. 10. 2014, 11:28:11 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Map;

import cz.vutbr.fit.layout.impl.DefaultBoxTreeBuilder;

/**
 * This class represents a box in the rendered page tree. It may contain a text or a content
 * object (e.g. an image).
 * 
 * @author burgetr
 */
public interface Box extends ContentRect, GenericTreeNode<Box>
{

    /**
     * Gets the order of precedence of the box in the page. A box that precedes another box in the
     * page must have a lower order number.
     * @return the document order for the box
     */
    public int getOrder();
    
    /**
     * Obtains the box text color.
     * @return the text color
     */
    public Color getColor();

    /**
     * Obtains the font family name used by the box.
     * @return The font family name.
     */
    public String getFontFamily();
    
    /**
     * Obtains the complete text contained in this box. For the leaf boxes,
     * it returns the corresponding string. For non-leaft boxes, it returns
     * the concatentaion of the leaf box strings.
     * @return a text string (possibly empty)
     */
    public String getText();
    
    /**
     * Obtains the text contained directly in this box without considering its child boxes.
     * @return a text string for leaf boxes, {@code null} for non-leaf boxes
     */
    public String getOwnText();
    
    /**
     * Obtains the content object contained in this box.
     * @return the content object or {@code null} when there is no object contained.
     */
    public ContentObject getContentObject();
    
    //=============================================================================================
    // BOUNDS
    //=============================================================================================
    
    /**
     * Gets the parent box of this box as obtained from the renderer. 
     * The renturned value may be {@code null} when the box was not obtained by rendering
     * (e.g. a box tree loaded from the storage).
     * @return the intrinsic parent box or {@code null}
     */
    public Box getIntrinsicParent();
    
    /**
     * Returns the bounds of the box as obtained from the renderer. This should correspond to the
     * border bounds of the box in the CSS terminology. This value is used when a box tree is
     * being constructed from individual boxes obtained from the renderer (e.g. using {@link DefaultBoxTreeBuilder}).
     * The renturned value may be {@code null} when the box was not obtained by rendering
     * (e.g. a box tree loaded from the storage).
     * @return the intrinsic bounds of the box or {@code null} when not available
     */
    public Rectangular getIntrinsicBounds();
    
    /** 
     * Returns the logical bounds of the box. Normally, the bounds are the same
     * as the content bounds. However, the Box may be extended
     * in order to enclose all the overlapping boxes.
     * @return the logical bounds of the box
     */
    public Rectangular getBounds();
    
    /**
     * Returns the content bounds. They correspond to the background bounds of the box;
     * however, when a border is present, it is included in the contents. Moreover,
     * the box is clipped by its clipping box.
     * @return the content bounds of the box
     */
    public Rectangular getContentBounds();

    /**
     * Sets the content bounds of the box.
     * @param visualBounds the new content bounds.
     */
    public void setContentBounds(Rectangular contentBounds);
    
    /**
     * Returns the bounds of the box as they visually appear to the user.
     * @return the visual bounds
     */
    public Rectangular getVisualBounds();

    /**
     * Sets the visual bounds of the box.
     * @param visualBounds the new visual bounds.
     */
    public void setVisualBounds(Rectangular visualBounds);
    
    /**
     * Returns the bounds of a substring of the own text of the box starting at {@code startPos} and ending at {@code endPos}.
     * @param startPos the starting position in the own text of the box (as obtained by )
     * @param endPos the ending position in the own text of the box
     * @return the visual bounds in pixels or {@code null} when there is no own text contained in the box
     */
    public Rectangular getSubstringBounds(int startPos, int endPos);
    
    /**
     * Checks whether the box is visible.
     * @return {@code true} when the box is visible
     */
    public boolean isVisible();
    
    /**
     * Checks if the box is separated from other contents by some visual means: a visible border
     * at any side, visible background different from its parent or a visible content.
     * @return {@code true} if the box is visually separated
     */
    public boolean isVisuallySeparated();

    //=============================================================================================
    // BOX TYPE
    //=============================================================================================
    
    /**
     * Resurns the type of the box.
     * @return the box type
     */
    public Type getType();
    
    /**
     * The type of the box.
     * 
     * @author burgetr
     */
    public enum Type 
    {
        /** Boxes produced by a DOM element */
        ELEMENT,
        /** Text boxes (produced by DOM text node */
        TEXT_CONTENT,
        /** Replaced boxes (images, objects, etc.) */
        REPLACED_CONTENT
    }
    
    //=============================================================================================
    // DOM RELATIONSHIP
    //=============================================================================================
    
    /**
     * Obtains a unique identification of the box source (e.g. the source DOM node).
     * This may be used for recognizing the boxes generated by the same DOM node.
     */
    public int getSourceNodeId();
    
    /**
     * When the box is generated by a DOM element, obtains the corresponding tag name.
     * @return the tag name or {@code null} for boxes that do not correspond to a DOM element.
     */
    public String getTagName();

    /**
     * Obtains a value of an additional attribute. The existing attribute names depend on the box implementation;
     * they may correspond to HTML (DOM) attributes or they may be purely virtual. The typical attributes to be
     * implemented are {@code class} or {@code href}.
     * @param name the attribute name
     * @return the attribute value or {@code null} if the value is not specified.
     */
    public String getAttribute(String name);
    
    /**
     * Obtains the names and values of all the box attributes. The existing attribute names depend on the box implementation;
     * they may correspond to HTML (DOM) attributes or they may be purely virtual. The typical attributes to be
     * implemented are {@code class} or {@code href}. 
     * @return A map assigning values to the individual attribute names. An empty map is returned when no attributes are defined.
     */
    public Map<String, String> getAttributes();
    
    /**
     * Obtains the display type of the element according to the CSS display: property.
     * @return The display type of NULL if the box does not correspond to an element
     */
    public DisplayType getDisplayType();
    
    /**
     * CSS Display Type
     * 
     * @author burgetr
     */
    public enum DisplayType
    {
        NONE,         
        INLINE, BLOCK, LIST_ITEM, INLINE_BLOCK, RUN_IN,
        TABLE, INLINE_TABLE, TABLE_ROW_GROUP, TABLE_HEADER_GROUP, TABLE_FOOTER_GROUP,
        TABLE_ROW, TABLE_COLUMN_GROUP, TABLE_COLUMN, TABLE_CELL, TABLE_CAPTION
    }
    
    
}
