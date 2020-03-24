/**
 * Area.java
 *
 * Created on 17. 10. 2014, 11:33:36 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;
import java.util.Vector;

/**
 * An area containing several visual boxes.
 * 
 * @author burgetr
 */
public interface Area extends ContentRect, GenericTreeNode<Area>, Taggable
{
    
    /**
     * Sets the name of the area that may be later used for its identification using {@link #getName()}.
     * @param name the name to be set
     */
    public void setName(String name);
    
    /**
     * Obtains the area name.
     * @return the area name set previously using {@link #setName(String)} or a default name when nothing has been previously set.
     */
    public String getName();
    
    /**
     * Obtains the tree the node belongs to.
     * @return the tree or {@code null} when the node does not form part of any tree.
     */
    public AreaTree getAreaTree();
    
    public void setAreaTree(AreaTree tree);
    
    /**
     * Returns the list of boxes that belong directly to this area.
     * @return the list of boxes (possibly empty)
     */
    public Vector<Box> getBoxes();
    
    /** 
     * Obtains all the boxes from this area and all the child areas.
     * @return The list of boxes
     */
    public Vector<Box> getAllBoxes();
    
    /**
     * Returns the complete text contained in this area and its sub area.
     * @return A text string (possibly empty)
     */
    public String getText();
    
    /**
     * Returns the complete text contained in this area and its sub area. The individual
     * areas are separated by the given string separator.
     * @param separator the string separating the individual areas
     * @return A text string (possibly empty)
     */
    public String getText(String separator);
    
    /**
     * Checks whether this area is formed by replaced boxes.
     * @return {@code true} if the area contains replaced boxes only
     */
    public boolean isReplaced();
    
    /**
     * Returns the topology of this area. 
     * @return The area topology.
     */
    public AreaTopology getTopology();
    
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
    
    /**
     * Updates the topologies of the child areas. This should be called when
     * some nodes have been inserted, removed or changed in this area.
     */
    public void updateTopologies();
    
    /**
     * Obtains the effective background color visible under the area.
     * @return The background color.
     */
    public Color getEffectiveBackgroundColor();

    /**
     * Checks whether the area can be interpreted as a horizontal separator.
     * @return {@code true} when this area is a horizontal separator
     */
    public boolean isHorizontalSeparator();

    /**
     * Checks whether the area can be interpreted as a vertical separator.
     * @return {@code true} when this area is a vertical separator
     */
    public boolean isVerticalSeparator();
    
    /**
     * Checks whether the area can be interpreted as any kind of separator.
     * @return {@code true} when this area is a separator
     */
    public boolean isSeparator();
    
    /**
     * Creates a new subarea from a specified region of the area and moves the selected child
     * nodes to the new area.
     * @param gp the subarea bounds
     * @param selected nodes to be moved to the new area
     * @param name the name (identification) of the new area
     * @return the new AreaNode created in the tree or null, if nothing was created
     */ 
    public Area createSuperArea(Rectangular gp, List<Area> selected, String name);

    /**
     * Inserts a new area as a new parent of the given child area. The given area is replaced 
     * by the new parent and it becomes a child area of the parent.
     * @param newParent the new parent area (replacement)
     * @param child the child area that should be replaced
     */ 
    public void insertParent(Area newParent, Area child);

    /**
     * Creates a copy of the area and makes it a next sibling of the source area.
     * @return the new area
     */
    public Area copy();
    
}
