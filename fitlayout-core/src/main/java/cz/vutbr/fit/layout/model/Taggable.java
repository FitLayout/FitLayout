/**
 * Taggable.java
 *
 * Created on 19. 3. 2015, 13:04:39 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Map;
import java.util.Set;

/**
 * An object that can be assigned tags.
 * 
 * @author burgetr
 */
public interface Taggable
{
    
    /**
     * Obtains the list of tags assigned to this area and their support.
     * @return the map of tags and their support (possibly empty)
     */
    public Map<Tag, Float> getTags();

    /**
     * Adds a tag to this area. If the tag is already assigned to the area, the greater of the
     * original and new support will be used.
     * @param tag The tag to be added.
     * @param support The assigned tag support from 0.0 to 1.0
     */
    public void addTag(Tag tag, float support);
    
    /**
     * Removes the given tag from the area.
     * @param tag the tag to be removed
     */
    public void removeTag(Tag tag);
    
    /**
     * Tests whether the area has this tag.
     * @param tag the tag to be tested.
     * @return <code>true</code> if the area has this tag
     */
    public boolean hasTag(Tag tag);
    
    /**
     * Tests whether the area has this tag with a support greater or equal to the specified value.
     * @param tag the tag to be tested
     * @param minSupport minimal required support
     * @return <code>true</code> if the area has this tag
     */
    public boolean hasTag(Tag tag, float minSupport);
    
    /**
     * Obtains all the tags with the support greater or equal to the specified value.
     * @param minSupport minimal required support
     * @return a set of tags with at least the minimal support (possibly empty)
     */
    public Set<Tag> getSupportedTags(float minSupport);
    
    /**
     * Obtains the support of the given tag assignment
     * @param tag The tag to be tested
     * @return The support of the given tag in the range 0.0 to 1.0. Returns 0.0 when the tag is not assigned
     * to this area.
     */
    public float getTagSupport(Tag tag);
    
    /**
     * Obtains the tag with the greatest support that is assigned to this area.
     * @return The tag with the greatest support or {@code null} if there are no tags assigned to this area
     */
    public Tag getMostSupportedTag();

}
