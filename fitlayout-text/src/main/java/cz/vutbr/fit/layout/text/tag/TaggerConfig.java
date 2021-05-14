/**
 * TaggerConfig.java
 *
 * Created on 13. 5. 2021, 21:02:46 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.Map;

import cz.vutbr.fit.layout.model.Tag;

/**
 * A tagger configuration and their assignment for tags.
 * 
 * @author burgetr
 */
public interface TaggerConfig
{
    
    /**
     * Returns the complete mapping of tags to their source taggers.
     * @return A map assigning a tagger to each tag
     */
    public Map<Tag, Tagger> getTaggers();
    
    /**
     * Finds a tagger for the given tag.
     * @param tag the given tag
     * @return The assigned tagger or {@code null} when no tagger is assigned.
     */
    public Tagger getTaggerForTag(Tag tag);
    
}
