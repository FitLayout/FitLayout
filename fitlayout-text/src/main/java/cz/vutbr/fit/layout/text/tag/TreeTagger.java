/**
 * TreeTagger.java
 *
 * Created on 11.11.2011, 12:56:50 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.Collection;
import java.util.Map;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;


/**
 * This class implements the area tree tagging.
 *
 * @author burgetr
 */
public class TreeTagger
{
    private static final float MIN_SUPPORT = 0.01f; //minimal returned support to assign the tag at all
    
    protected Area root;
    protected Map<Tag, Tagger> taggers;
    
    public TreeTagger(Area root, Map<Tag, Tagger> taggers)
    {
        this.root = root;
        this.taggers = taggers;
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
        for (Tag tag : taggers.keySet())
        {
            final Tagger t = taggers.get(tag);
            float support = t.belongsTo(area); 
            if (support > MIN_SUPPORT)
                area.addTag(tag, support);
        }
    }
    
}
