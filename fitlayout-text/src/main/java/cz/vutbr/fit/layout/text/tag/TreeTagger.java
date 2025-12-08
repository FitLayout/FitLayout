/**
 * TreeTagger.java
 *
 * Created on 11.11.2011, 12:56:50 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.MultiTagger;
import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;


/**
 * This class implements the area tree tagging.
 *
 * @author burgetr
 */
public class TreeTagger
{
    private static Logger log = LoggerFactory.getLogger(TreeTagger.class);
    private static final float MIN_SUPPORT = 0.01f; //minimal returned support to assign the tag at all
    
    private Area root;
    private TaggerConfig tc;
    private Map<Tag, Tagger> taggers;
    private Map<Tagger, List<Tag>> tagAssignment;
    
    public TreeTagger(Area root, TaggerConfig tc)
    {
        this.root = root;
        this.tc = tc;
        this.taggers = tc.getTaggers();
        // Group the assigned tags by taggers because some taggers may assign multiple tags
        this.tagAssignment = createTagAssignmentMap();
    }
    
    /**
     * Obtains the collection of all tags used by the taggers
     * @return the collection of tags
     */
    public Collection<Tag> getAllTags()
    {
        return taggers.keySet();
    }
    
    /**
     * Applies all the taggers to the whole tree.
     */
    public void tagTree()
    {
        tagSubtree(root);
    }

    /**
     * Applies all the taggers a subtree of the area tree.
     * @param root the root node of the subtree
     */
    public void tagSubtree(Area root)
    {
        tagSingleNode(root);
        for (int i = 0; i < root.getChildCount(); i++)
            tagSubtree(root.getChildAt(i));
    }
    
    /**
     * Applies all the taggers to a single tree node.
     * @param area the tree node
     */
    public void tagSingleNode(Area area)
    {
        for (Map.Entry<Tagger, List<Tag>> entry : tagAssignment.entrySet())
        {
            final Tagger t = entry.getKey();
            final List<Tag> tags = entry.getValue();
            if (tags.size() == 1)
            {
                // a single tag assigned by the tagger
                float support = t.belongsTo(area);
                if (support > MIN_SUPPORT)
                    area.addTag(tags.get(0), support);
            }
            else
            {
                // the tagger assigns multiple tags -- we need to use discriminators
                if (t instanceof MultiTagger)
                {
                    MultiTagger mt = (MultiTagger) t;
                    Map<String, Float> rel = mt.getRelevances(area);
                    for (Tag tag : tags)
                    {
                        List<String> discrs = tc.getDiscriminatorsForTag(tag);
                        for (String d : discrs)
                        {
                            float relSupport = rel.getOrDefault(d, 0.0f);
                            if (relSupport > MIN_SUPPORT)
                                area.addTag(tag, relSupport);
                        }
                    }
                }
            }
        }
        
        
        for (Tag tag : taggers.keySet())
        {
            final Tagger t = taggers.get(tag);
            float support = t.belongsTo(area); 
            if (support > MIN_SUPPORT)
                area.addTag(tag, support);
        }
    }
    
    private Map<Tagger, List<Tag>> createTagAssignmentMap()
    {
        Map<Tagger, List<Tag>> ret = new HashMap<>();
        for (Map.Entry<Tag, Tagger> entry : taggers.entrySet())
            ret.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        // check if the taggers used for multiple tags are actually multi-taggers
        for (Map.Entry<Tagger, List<Tag>> entry : ret.entrySet())
        {
            Tagger t = entry.getKey();
            if (entry.getValue().size() > 1 && !(t instanceof MultiTagger))
                log.warn("Tagger {} is used for multiple tags, but it is not a multi-tagger", t);
        }
        return ret;
    }
    
}
