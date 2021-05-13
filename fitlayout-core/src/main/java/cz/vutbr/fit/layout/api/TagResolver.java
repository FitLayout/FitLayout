/**
 * TagResolver.java
 *
 * Created on 29. 3. 2019, 12:54:36 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Set;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;

/**
 * A resolver that is able to assign a set of tags to an area.
 * 
 * @author burgetr
 */
public interface TagResolver
{

    /**
     * Assigns a set of tags to a given area based on its properties. The actual way of tag assignment
     * depends on the particular implementation.
     * 
     * @param a the area
     * @return a set of tags assigned to this area by the resolver (may be empty)
     */
    public Set<Tag> getAreaTags(Area a);
    
}
