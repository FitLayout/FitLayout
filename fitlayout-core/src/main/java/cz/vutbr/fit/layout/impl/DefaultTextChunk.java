/**
 * DefaultTextChunk.java
 *
 * Created on 26. 6. 2018, 13:52:54 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.model.TextStyle;

/**
 * A default text chunk implementation.
 * 
 * @author burgetr
 */
public class DefaultTextChunk extends DefaultContentRect implements TextChunk
{
    private ChunkSet chunkSet;
    private String text;
    private Area sourceArea;
    private Box sourceBox;
    private String fontFamily;
    private Color color;
    private Color effectiveBackgroundColor;
    private String name;
    
    /** Assigned tags */
    private Map<Tag, Float> tags;


    public DefaultTextChunk()
    {
        text = "";
        name = "<chunk>";
        setBounds(new Rectangular());
        tags = new HashMap<>();
        color = Color.BLACK;
        fontFamily = "";
    }
    
    public DefaultTextChunk(Rectangular r, Area sourceArea, Box sourceBox)
    {
        text = "";
        name = "<chunk>";
        this.sourceArea = sourceArea;
        this.sourceBox = sourceBox;
        setBounds(r);
        tags = new HashMap<>();
        //addBox(sourceBox); //the box is used for computing the text color of the area (e.g. in AreaStyle)
        copyStyle(sourceArea);
        color = sourceBox.getColor();
        fontFamily = sourceBox.getFontFamily();
    }

    @Override
    public ChunkSet getChunkSet()
    {
        return chunkSet;
    }

    public void setChunkSet(ChunkSet chunkSet)
    {
        this.chunkSet = chunkSet;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Area getSourceArea()
    {
        return sourceArea;
    }

    public void setSourceArea(Area sourceArea)
    {
        this.sourceArea = sourceArea;
        copyStyle(sourceArea);
    }

    @Override
    public Box getSourceBox()
    {
        return sourceBox;
    }
    
    public void setSourceBox(Box sourceBox)
    {
        this.sourceBox = sourceBox;
        setFontFamily(sourceBox.getFontFamily());
        setColor(sourceBox.getColor());
    }

    public String getFontFamily()
    {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily)
    {
        this.fontFamily = fontFamily;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    @Override
    public Color getEffectiveBackgroundColor()
    {
        return effectiveBackgroundColor;
    }

    public void setEffectiveBackgroundColor(Color effectiveBackgroundColor)
    {
        this.effectiveBackgroundColor = effectiveBackgroundColor;
    }

    //====================================================================================
    // tagging
    //====================================================================================
    
    @Override
    public void addTag(Tag tag, float support)
    {
        final Float oldsupport = tags.get(tag);
        if (oldsupport == null || oldsupport < support)
            tags.put(tag, support);
    }
    
    @Override
    public boolean hasTag(Tag tag)
    {
        return tags.get(tag) != null;
    }
    
    @Override
    public boolean hasTag(Tag tag, float minSupport)
    {
        final Float sp = tags.get(tag); 
        return (sp != null && sp >= minSupport);
    }

    @Override
    public Set<Tag> getSupportedTags(float minSupport)
    {
        Set<Tag> ret = new HashSet<Tag>();
        for (Map.Entry<Tag, Float> entry : tags.entrySet())
        {
            if (entry.getValue() >= minSupport)
                ret.add(entry.getKey());
        }
        return ret;
    }

    @Override
    public float getTagSupport(Tag tag)
    {
        Float f = tags.get(tag);
        if (f == null)
            return 0.0f;
        else
            return f;
    }
    
    @Override
    public Tag getMostSupportedTag()
    {
        float max = -1.0f;
        Tag ret = null;
        for (Map.Entry<Tag, Float> entry : tags.entrySet())
        {
            if (entry.getValue() > max)
            {
                max = entry.getValue();
                ret = entry.getKey();
            }
        }
        return ret;
    }
    
    /**
     * Removes all tags that belong to the given collection.
     * @param c A collection of tags to be removed.
     */
    public void removeAllTags(Collection<Tag> c)
    {
        for (Tag t : c)
            tags.remove(t);
    }
    
    /**
     * Removes the specific tag
     * @param tag
     */
    @Override
    public void removeTag(Tag tag) 
    {
        tags.remove(tag);   
    }
    
    /**
     * Obtains the set of tags assigned to the area.
     * @return a set of tags
     */
    @Override
    public Map<Tag, Float> getTags()
    {
        return tags;
    }
    
    //=============================================================================================
    
    protected void copyStyle(Area src)
    {
        setBackgroundColor((src.getBackgroundColor() == null) ? null : new Color(src.getBackgroundColor().getRed(), src.getBackgroundColor().getGreen(), src.getBackgroundColor().getBlue()));
        setEffectiveBackgroundColor(src.getEffectiveBackgroundColor());
        setBackgroundSeparated(src.isBackgroundSeparated());
        setTextStyle(new TextStyle(src.getTextStyle()));
        setBackgroundSeparated(src.isBackgroundSeparated());
    }
    
    //=============================================================================================

    @Override
    public String toString()
    {
        return "<chunk> '" + getText() + "'";
    }
    
}
