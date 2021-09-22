/**
 * CombinedTagger.java
 *
 * Created on 1. 4. 2019, 14:28:54 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TagOccurrence;


/**
 * A tagger that combines multiple taggers together (in the order of their preference).
 * 
 * @author burgetr
 */
public abstract class CombinedTagger extends BaseTagger
{
    private LinkedHashMap<Tagger, Float> subTaggers;
    
    
    public CombinedTagger()
    {
        subTaggers = new LinkedHashMap<Tagger, Float>();
    }

    public LinkedHashMap<Tagger, Float> getSubTaggers()
    {
        return subTaggers;
    }
    
    public void addTagger(Tagger tagger, float weight)
    {
        subTaggers.put(tagger, weight);
    }
    
    @Override
    public float belongsTo(Area node)
    {
        for (Map.Entry<Tagger, Float> entry : subTaggers.entrySet())
        {
            final Tagger t = entry.getKey();
            float support = t.belongsTo(node);
            if (support > 0.01f)
                return entry.getValue() * support;
        }
        return 0.0f;
    }
    
    @Override
    public List<TagOccurrence> extract(String src)
    {
        for (Map.Entry<Tagger, Float> entry : subTaggers.entrySet())
        {
            final Tagger t = entry.getKey();
            List<TagOccurrence> occlist = t.extract(src);
            if (!occlist.isEmpty())
                return occlist;
        }
        return Collections.emptyList();
    }
    
}
