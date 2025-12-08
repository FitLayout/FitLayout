/**
 * FilteredTaggerConfig.java
 *
 * Created on 7. 12. 2025, 11:47:28 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.model.Tag;
import java.util.stream.Collectors;

/**
 * A tagger config implementation that filters out specific tags based on their names.
 * 
 * @author burgetr
 */
public class FilteredTaggerConfig implements TaggerConfig
{
    private Set<String> disabledTagNames;
    private TaggerConfig src;
    
    public FilteredTaggerConfig(TaggerConfig src, Set<String> disabledTagNames)
    {
        this.disabledTagNames = disabledTagNames;
        this.src = src;
    }

    @Override
    public Map<Tag, Tagger> getTaggers()
    {
        return src.getTaggers().entrySet().stream()
               .filter(e -> !disabledTagNames.contains(e.getKey().getName()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Tagger getTaggerForTag(Tag tag)
    {
        if (!disabledTagNames.contains(tag.getName()))
            return src.getTaggerForTag(tag);
        else
            return null;
    }

    @Override
    public List<String> getDiscriminatorsForTag(Tag tag)
    {
        if (!disabledTagNames.contains(tag.getName()))
            return src.getDiscriminatorsForTag(tag);
        else
            return List.of();
    }

}
