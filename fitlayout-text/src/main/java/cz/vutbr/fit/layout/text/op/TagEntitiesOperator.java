/**
 * TagEntitiesOperator.java
 *
 * Created on 22. 1. 2015, 16:02:09 by burgetr
 */
package cz.vutbr.fit.layout.text.op;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ScriptObject;
import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.text.tag.TreeTagger;


/**
 * 
 * @author burgetr
 */
public class TagEntitiesOperator extends BaseOperator implements ScriptObject
{
    private static final String PARAM_PREFIX = "tag";

    private static Logger log = LoggerFactory.getLogger(TagEntitiesOperator.class);

    private TreeTagger tagger;
    private Map<Tag, Tagger> usedTaggers;
    private Set<String> disabledTaggers;

    
    public TagEntitiesOperator()
    {
        usedTaggers = new HashMap<>();
        disabledTaggers = new HashSet<>();
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
        List<Parameter> ret = new ArrayList<>(usedTaggers.size());
        for (Tagger tagger : usedTaggers.values())
            ret.add(new ParameterBoolean(PARAM_PREFIX + tagger.getName()));
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
                disabledTaggers.remove(tname);
            else
                disabledTaggers.add(tname);
            return true;
        }
        else
            return false;
    }

    @Override
    public Object getParam(String name)
    {
        if (name.startsWith(PARAM_PREFIX))
            return !disabledTaggers.contains(name.substring(PARAM_PREFIX.length()));
        else
            return false;
    }

    /**
     * Registers a new tagger that should be used by this operator for assigning a tag.
     * @param tag the tag to be assigned
     * @param tagger the tagger instance to be added
     */
    public void addTagger(Tag tag, Tagger tagger)
    {
        usedTaggers.put(tag, tagger);
    }
    
    /**
     * Registers a map of taggers that should be used by this operator.
     * @param taggers the map of taggers to be used for assigning the tags
     */
    public void setTaggers(Map<Tag, Tagger> taggers)
    {
        usedTaggers = taggers;
    }
    
    /**
     * Unregisters all taggers from the operator.
     */
    public void clearTaggers()
    {
        usedTaggers.clear();
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
        if (usedTaggers.isEmpty())
            log.warn("Applying TagEntitiesOperator with no taggers configured");
        // collect enabled taggers
        Map<Tag, Tagger> activeTaggers = new HashMap<>();
        for (var entry : usedTaggers.entrySet())
        {
            if (!disabledTaggers.contains(entry.getValue().getName()))
                activeTaggers.put(entry.getKey(), entry.getValue());
        }
        // perform tagging
        tagger = new TreeTagger(root, activeTaggers);
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
