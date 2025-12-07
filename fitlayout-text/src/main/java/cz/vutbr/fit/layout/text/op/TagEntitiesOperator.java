/**
 * TagEntitiesOperator.java
 *
 * Created on 22. 1. 2015, 16:02:09 by burgetr
 */
package cz.vutbr.fit.layout.text.op;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ScriptObject;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.text.tag.FilteredTaggerConfig;
import cz.vutbr.fit.layout.text.tag.FixedTaggerConfig;
import cz.vutbr.fit.layout.text.tag.TreeTagger;


/**
 * 
 * @author burgetr
 */
public class TagEntitiesOperator extends BaseOperator implements ScriptObject
{
    private static final String PARAM_PREFIX = "tag_";

    private static Logger log = LoggerFactory.getLogger(TagEntitiesOperator.class);

    private TreeTagger tagger;
    private TaggerConfig taggerConfig;
    private Set<String> disabledTags;

    
    public TagEntitiesOperator()
    {
        disabledTags = new HashSet<>();
        // Use an empty tagger config for start. This will be replaced with the actual tagger config
        // using setTaggerConfig().
        taggerConfig = new FixedTaggerConfig(); 
    }
    
    public TaggerConfig getTaggerConfig()
    {
        return taggerConfig;
    }

    public void setTaggerConfig(TaggerConfig taggerConfig)
    {
        this.taggerConfig = taggerConfig;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tag.Entities";
    }
    
    @Override
    public String getName()
    {
        return "Tag entities";
    }

    @Override
    public String getDescription()
    {
        return "Recognizes entities in area text using different taggers"
                + " and adds the corresponding tags to the areas.";
    }

    @Override
    public String getCategory()
    {
        return "Classification";
    }

    @Override
    public List<Parameter> defineParams()
    {
        final var definedTags = taggerConfig.getTaggers().keySet();
        List<Parameter> ret = new ArrayList<>(definedTags.size());
        for (Tag tag : definedTags)
        {
            String pname = tag.getName();
            ret.add(new ParameterBoolean(PARAM_PREFIX + pname));
        }
        return ret;
    }

    @Override
    public boolean setParam(String name, Object value)
    {
        if (name.startsWith(PARAM_PREFIX) && value instanceof Boolean)
        {
            String tname = name.substring(PARAM_PREFIX.length());
            Boolean val = (Boolean) value;
            if (val)
                disabledTags.remove(tname);
            else
                disabledTags.add(tname);
            return true;
        }
        else
            return false;
    }

    @Override
    public Object getParam(String name)
    {
        if (name.startsWith(PARAM_PREFIX))
            return !disabledTags.contains(name.substring(PARAM_PREFIX.length()));
        else
            return false;
    }

    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        var usedTaggers = taggerConfig.getTaggers();
        if (usedTaggers.isEmpty())
            log.warn("Applying TagEntitiesOperator with no taggers configured");
        TaggerConfig filteredConfig = new FilteredTaggerConfig(taggerConfig, disabledTags);
        // perform tagging
        tagger = new TreeTagger(root, filteredConfig);
        tagger.tagTree();
    }

    @Override
    public String getVarName()
    {
        return "entities";
    }

    @Override
    public void setIO(Reader in, Writer out, Writer err)
    {
    }

}
