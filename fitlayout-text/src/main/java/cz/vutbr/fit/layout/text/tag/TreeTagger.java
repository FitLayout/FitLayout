/**
 * TreeTagger.java
 *
 * Created on 11.11.2011, 12:56:50 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<String> multiTaggers; // multi-tagger IDs
    
    
    public TreeTagger(Area root, TaggerConfig tc)
    {
        this.root = root;
        this.tc = tc;
        this.taggers = tc.getTaggers();
        checkMultiTaggers();
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
        // start tagging for all taggers
        for (Tagger t : tagAssignment.keySet())
            t.startSubtree(root);
        
        // tag the whole tree
        tagSubtree(root);
        
        // finish tagging for all taggers
        for (Tagger t : tagAssignment.keySet())
            t.finishSubtree(root);
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
            if (!multiTaggers.contains(t.getId()))
            {
                // no discriminators, assign all tags (usually one)
                float support = t.belongsTo(area);
                if (support > MIN_SUPPORT)
                {
                    for (Tag tag : tags)
                    {
                        area.addTag(tag, support);
                    }
                }
            }
            else
            {
                // discriminators used - the tagger assigns multiple tags (must be a multi-tagger)
                if (t instanceof MultiTagger)
                {
                    MultiTagger mt = (MultiTagger) t;
                    Map<String, Float> rel = mt.getRelevances(area);
                    for (Tag tag : tags)
                    {
                        List<String> discrs = tc.getDiscriminatorsForTag(tag);
                        float maxRelevance = 0.0f;
                        for (String d : discrs)
                        {
                            float relSupport = rel.getOrDefault(d, 0.0f);
                            if (relSupport > maxRelevance)
                                maxRelevance = relSupport;
                        }
                        if (maxRelevance > MIN_SUPPORT)
                            area.addTag(tag, maxRelevance);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if the taggers that use discriminators are actually multi-taggers.
     */
    private void checkMultiTaggers()
    {
        multiTaggers = new HashSet<>();
        for (Map.Entry<Tag, Tagger> entry : taggers.entrySet())
        {
            Tag tag = entry.getKey();
            Tagger tagger = entry.getValue();
            if (!tc.getDiscriminatorsForTag(tag).isEmpty())
            {
                if (tagger instanceof MultiTagger)
                    multiTaggers.add(tagger.getId());
                else
                    log.error("Tagger {} is used for tag {} with discriminators, but it is not a multi-tagger", tagger, tag);
            }
        }
    }
    
    private Map<Tagger, List<Tag>> createTagAssignmentMap()
    {
        Map<Tagger, List<Tag>> ret = new HashMap<>();
        for (Map.Entry<Tag, Tagger> entry : taggers.entrySet())
            ret.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        return ret;
    }
    
}
