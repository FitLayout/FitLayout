/**
 * Area.java
 *
 * Created on 17. 10. 2014, 11:33:36 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

import cz.vutbr.fit.layout.model.Border.Side;

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
     * Adds a new box to the area.
     * @param box the box to add
     */
    public void addBox(Box box);
    
    /**
     * Returns the list of boxes that belong directly to this area.
     * @return the list of boxes (possibly empty)
     */
    public List<Box> getBoxes();
    
    /** 
     * Obtains all the boxes from this area and all the child areas.
     * @return The list of boxes
     */
    public List<Box> getAllBoxes();
    
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
     * Area grouping level. Area level 0 corresponds to the areas formed by boxes, greater numbers represent
     * greater level of grouping (artificial areas).
     * @return the area level
     */
    public int getLevel();

    /**
     * Sets the area level. Area level 0 corresponds to the areas formed by boxes, greater numbers represent
     * greater level of grouping (artificial areas).
     * @param level the new level to set.
     */
    public void setLevel(int level);

    /**
     * Sets the style of the box border at the given side.
     * @param side the border side.
     * @param style the new border style
     */
    public void setBorderStyle(Side side, Border style);
    
    /**
     * Returns the topology of this area. 
     * @return The area topology.
     */
    public AreaTopology getTopology();

    /**
     * Sets the grid position of this area within the parent topology.
     * @param gp the new grid position
     */
    public void setGridPosition(Rectangular gp);
    
    /**
     * Gets the grid position of this area within the parent topology.
     * @return the grid position or a unit rectangle when there is no parent
     */
    public Rectangular getGridPosition();
    
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
     * When set to true, the area is considered to be separated from other
     * areas explicitly, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @return <code>true</code>, if the area is explicitly separated
     */
    public boolean isExplicitlySeparated();

    /**
     * When set to true, the area is considered to be separated from other
     * areas explicitly, i.e. independently on its real borders or background.
     * This is usually used for some new superareas.
     * @param explicitlySeparated <code>true</code>, if the area should be explicitly separated
     */
    public void setExplicitlySeparated(boolean explicitlySeparated);
    
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
