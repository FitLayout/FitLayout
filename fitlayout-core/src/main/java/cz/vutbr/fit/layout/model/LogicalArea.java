/**
 * LogicalArea.java
 *
 * Created on 19. 3. 2015, 12:57:46 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

/**
 * Logical area represents a set of areas that form a single semantic entity. Logical areas are organized
 * in a tree where the parent-child relationships have some semantic meaning instead of representing
 * the actual layout.
 * 
 * @author burgetr
 */
public interface LogicalArea extends GenericTreeNode<LogicalArea>
{

    public void addArea(Area a);
    
    public List<Area> getAreas();
    
    public Area getFirstArea();
    
    public int getAreaCount();
    
    public void setText(String text);
    
    public String getText();
    
    public void setMainTag(Tag tag);
    
    public Tag getMainTag();
    
    /**
     * Scans a logical area subtree rooted in this logical area for the given area.
     * @param area the layout area to search for
     * @return the deepest logical area that contains the given area or {@code null} when the given area
     * is not present in the given subtree
     */
    public LogicalArea findArea(Area area);
    
}
