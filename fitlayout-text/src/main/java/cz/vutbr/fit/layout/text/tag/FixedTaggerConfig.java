/**
 * FixedTaggerConfig.java
 *
 * Created on 13. 5. 2021, 21:06:45 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.fit.layout.model.Tag;

/**
 * A tagger config implementation based on a fixed map.
 * 
 * @author burgetr
 */
public class FixedTaggerConfig implements TaggerConfig
{
    /**
     * Assigns a tagger to each tag.
     */
    private Map<Tag, Tagger> taggers;

    public FixedTaggerConfig()
    {
        taggers = new HashMap<>();
    }
    
    @Override
    public Map<Tag, Tagger> getTaggers()
    {
        return taggers;
    }

    @Override
    public Tagger getTaggerForTag(Tag tag)
    {
        return taggers.get(tag);
    }

    public void setTagger(Tag tag, Tagger tagger)
    {
        taggers.put(tag, tagger);
    }
    
}
