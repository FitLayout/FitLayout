/**
 * TagByAttributeOperator.java
 *
 * Created on 9. 10. 2023, 14:57:59 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Tag;

/**
 * Tags visual area based on a chosen DOM attribute value.
 * 
 * @author burgetr
 */
public class TagByAttributeOperator extends BaseOperator
{
    private String attrName;
    private String tagType;
    private boolean multiTag;
    

    public TagByAttributeOperator()
    {
        super();
        attrName = "class";
        tagType = "attr";
        multiTag = true;
    }

    public TagByAttributeOperator(String attrName, String tagType, boolean multiTag)
    {
        super();
        this.attrName = attrName;
        this.tagType = tagType;
        this.multiTag = multiTag;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tag.Attribute";
    }
    
    @Override
    public String getName()
    {
        return "Tag areas by DOM attribute";
    }

    @Override
    public String getDescription()
    {
        return "Tags visual areas based on a chosen DOM attribute value.";
    }

    @Override
    public String getCategory()
    {
        return "Classification";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterString("attrName", "DOM attribute name", 1, 255));
        ret.add(new ParameterString("tagType", "The tag type to be used for the created tags", 1, 255));
        ret.add(new ParameterBoolean("multiTag", "Assign multiple tags when the attribute contains multiple space-separated values"));
        return ret;
    }
    
    //==============================================================================

    public String getAttrName()
    {
        return attrName;
    }

    public void setAttrName(String attrName)
    {
        this.attrName = attrName;
    }

    public String getTagType()
    {
        return tagType;
    }

    public void setTagType(String tagType)
    {
        this.tagType = tagType;
    }

    public boolean isMultiTag()
    {
        return multiTag;
    }

    public void setMultiTag(boolean multiTag)
    {
        this.multiTag = multiTag;
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
        Map<String, Tag> assignedTags = new HashMap<>();
        recursivelyAssignTags(root, assignedTags);
    }

    private void recursivelyAssignTags(Area root, Map<String, Tag> assignedTags)
    {
        assignTags(root, assignedTags);
        for (Area child : root.getChildren())
            recursivelyAssignTags(child, assignedTags);
    }
    
    private void assignTags(Area a, Map<String, Tag> assignedTags)
    {
        // find a box with the given attribute
        Box box = null;
        for (Box cand : a.getBoxes())
        {
            if (cand.getAttribute(attrName) != null)
            {
                box = cand;
                break;
            }
        }
        // assign tag(s)
        if (box != null)
        {
            String attrVal = box.getAttribute(attrName).trim().toLowerCase();
            if (!attrVal.isEmpty())
            {
                if (multiTag)
                {
                    String[] tagVals = attrVal.split("\\s+");
                    for (String tagVal : tagVals)
                        assignTag(a, tagVal, assignedTags);
                }
                else
                {
                    assignTag(a, attrVal, assignedTags);
                }
            }
        }
    }
    
    /**
     * Assigns a tag with the given name to the given area. Tries to reuse tags provided in assignedTags.
     * 
     * @param a
     * @param tagName
     * @param assignedTags
     */
    private void assignTag(Area a, String tagName, Map<String, Tag> assignedTags)
    {
        Tag tag = assignedTags.get(tagName);
        if (tag == null)
        {
             tag = createTag(tagName);
             assignedTags.put(tagName, tag);
        }
        a.addTag(tag, 1.0f);
    }
    
    protected Tag createTag(String tagName)
    {
        final String normalizedName = tagName.replaceAll("[^a-zA-Z0-9]+", "_");
        return new DefaultTag(tagType, normalizedName);
    }
}
