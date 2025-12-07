/**
 * FixedTaggerConfig.java
 *
 * Created on 13. 5. 2021, 21:06:45 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.api.TaggerConfig;
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
    private Map<Tag, List<String>> discriminators;

    public FixedTaggerConfig()
    {
        taggers = new HashMap<>();
        discriminators = new HashMap<>();
    }
    
    @Override
    public Map<Tag, Tagger> getTaggers()
    {
        return taggers;
    }

    public Map<Tag, List<String>> getDiscriminators()
    {
        return discriminators;
    }

    @Override
    public Tagger getTaggerForTag(Tag tag)
    {
        return taggers.get(tag);
    }

    @Override
    public List<String> getDiscriminatorsForTag(Tag tag)
    {
        var ret = discriminators.get(tag);
        return ret == null ? List.of() : ret;
    }

    public void setTagger(Tag tag, Tagger tagger)
    {
        taggers.put(tag, tagger);
    }
    
    public void setDiscriminators(Tag tag, List<String> discriminators)
    {
        this.discriminators.put(tag, discriminators);
    }
    
}
