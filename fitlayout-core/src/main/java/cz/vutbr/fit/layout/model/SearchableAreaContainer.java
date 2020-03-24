/**
 * SearchableAreaContainer.java
 *
 * Created on 13.5.2011, 15:17:08 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

/**
 * A basic container of areas with the possibility of searching the areas.
 * 
 * @author burgetr
 */
public interface SearchableAreaContainer
{
    
    /**
     * Finds the bottom-most area node present at the given coordinates in the area tree.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the area node present at the given coordinates or <code>null</code> when no node is found.
     */
    abstract public Area getAreaAt(int x, int y);
    
    /**
     * Finds all the area nodes present at the given coordinates in the area tree.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return a list of the area nodes present at the given coordinates ordered from the topmost to the bottommost one.
     */
    abstract public List<Area> getAreasAt(int x, int y);
    
    /**
     * Finds an area by the given name.
     * @param name the name of the area
     * @return the area with the given name or <code>null</code> when nothing is found.
     */
    abstract public Area getAreaByName(String name);
    
}
