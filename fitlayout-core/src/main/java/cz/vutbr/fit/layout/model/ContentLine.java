/**
 * ContentLine.java
 *
 * Created on 8. 11. 2018, 13:39:52 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

/**
 * A sequence of areas representing the contents of a text line. It is generally
 * a list of areas that corresponds to a physical or logical line of contents.
 * 
 * @author burgetr
 */
public interface ContentLine extends List<ContentRect>
{

    /**
     * Finds the area that precedes the given area on the line.
     * @param area the given area
     * @return the preceding area or {@code null} if {@code area} is the first area in the line.
     */
    public ContentRect getAreaBefore(ContentRect area);
    
    /**
     * Finds the area that follows the given area on the line.
     * @param area the given area
     * @return the following area or {@code null} if {@code area} is the last area in the line.
     */
    public ContentRect getAreaAfter(ContentRect area);
    
}
