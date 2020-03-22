/**
 * DefaultBox.java
 *
 * Created on 21. 11. 2014, 9:45:18 by burgetr
 */
package org.fit.layout.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fit.layout.model.Box;
import org.fit.layout.model.ContentObject;
import org.fit.layout.model.Rectangular;

/**
 * Default generic box implementation.
 *  
 * @author burgetr
 */
public class DefaultBox extends DefaultContentRect<Box> implements Box
{
    private boolean visible;
    private Color color;
    private String fontFamily;
    private String text;
    private ContentObject contentObject;
    
    private Rectangular contentBounds;
    private Rectangular visualBounds;
    
    private int sourceNodeId;
    private String tagName;
    private Map<String, String> attributes;
    private Box.Type type;
    private Box.DisplayType displayType;
    
    public DefaultBox()
    {
        super(Box.class);
        visible = true;
        text = "";
        color = Color.BLACK;
        fontFamily = "none";
        tagName = "none";
        type = Type.ELEMENT;
        displayType = DisplayType.BLOCK;
    }
    
    public DefaultBox(DefaultBox src)
    {
        super(Box.class);
        visible = src.visible;
        color = new Color(src.color.getRed(), src.color.getGreen(), src.color.getBlue(), src.color.getAlpha());
        fontFamily = new String(src.fontFamily);
        text = new String(src.text);
        contentObject = src.contentObject;
        tagName = new String(src.tagName);
        if (src.attributes != null)
            attributes = new HashMap<String, String>(src.attributes);
        type = src.type;
        displayType = src.displayType;
    }
    
    @Override
    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    @Override
    public Color getColor()
    {
        return color;
    }
    
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    @Override
    public String getFontFamily()
    {
        return fontFamily;
    }
    
    public void setFontFamily(String fontFamily)
    {
        this.fontFamily = fontFamily;
    }
    
    @Override
    public String getText()
    {
        if (isLeaf())
        {
            return text;
        }
        else
        {
            String ret = "";
            for (int i = 0; i < getChildCount(); i++)
            {
                if (ret.trim().length() > 0)
                    ret += " ";
                ret = ret + getChildAt(i).getText().trim();
            }
            return ret;
        }
    }
    
    @Override
    public String getOwnText()
    {
        if (isLeaf())
            return text;
        else
            return null;
    }

    public void setText(String text)
    {
        this.text = text;
    }
    
    @Override
    public ContentObject getContentObject()
    {
        return contentObject;
    }
    
    public void setContentObject(ContentObject contentObject)
    {
        this.contentObject = contentObject;
    }
    
    @Override
    public Rectangular getContentBounds()
    {
        return contentBounds;
    }
    
    public void setContentBounds(Rectangular contentBounds)
    {
        this.contentBounds = contentBounds;
    }
    
    @Override
    public Rectangular getVisualBounds()
    {
        return visualBounds;
    }
    
    public void setVisualBounds(Rectangular visualBounds)
    {
        this.visualBounds = visualBounds;
    }
    
    @Override
    public Rectangular getSubstringBounds(int startPos, int endPos)
    {
        final String t = getOwnText();
        if (t != null) 
        {
            Rectangular ret = new Rectangular(getContentBounds());
            //no font information is available here so we just make a simple guess
            final float step = ret.getWidth() / (float) t.length();
            final int origin = ret.getX1();
            ret.setX1(origin + Math.round(startPos * step));
            ret.setX2(origin + Math.round(endPos * step));
            return ret;
        }
        else
            return null;
    }

    @Override
    public int getSourceNodeId()
    {
        return sourceNodeId;
    }

    public void setSourceNodeId(int sourceNodeId)
    {
        this.sourceNodeId = sourceNodeId;
    }

    @Override
    public String getTagName()
    {
        return tagName;
    }
    
    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }
    
    @Override
    public String getAttribute(String name)
    {
        if (attributes != null)
            return attributes.get(name);
        else
            return null;
    }
    
    @Override
    public Map<String, String> getAttributes()
    {
        if (attributes != null)
            return attributes;
        else
            return Collections.emptyMap();
    }

    public void setAttribute(String name, String value)
    {
        if (attributes == null)
            attributes = new HashMap<String, String>();
        attributes.put(name, value);
    }
    
    public void removeAttribute(String name)
    {
        if (attributes != null)
            attributes.remove(name);
    }
    
    @Override
    public Box.Type getType()
    {
        return type;
    }
    
    public void setType(Box.Type type)
    {
        this.type = type;
    }
    
    @Override
    public Box.DisplayType getDisplayType()
    {
        return displayType;
    }
    
    public void setDisplayType(Box.DisplayType displayType)
    {
        this.displayType = displayType;
    }
    
    @Override
    public int getX1()
    {
        return getVisualBounds().getX1();
    }

    @Override
    public int getY1()
    {
        return getVisualBounds().getY1();
    }

    @Override
    public int getX2()
    {
        return getVisualBounds().getX2();
    }

    @Override
    public int getY2()
    {
        return getVisualBounds().getY2();
    }

    @Override
    public int getWidth()
    {
        return getVisualBounds().getWidth();
    }

    @Override
    public int getHeight()
    {
        return getVisualBounds().getHeight();
    }

    @Override
    public void move(int xofs, int yofs)
    {
        getVisualBounds().move(xofs, yofs);
        getContentBounds().move(xofs, yofs);
        super.move(xofs, yofs);
    }

    @Override
    public String toString()
    {
        String ret = null;
        switch (type)
        {
            case ELEMENT:
                ret = "<" + getTagName();
                if (getAttribute("id") != null)
                    ret += " id=" + getAttribute("id");
                if (getAttribute("class") != null)
                    ret += " class=" + getAttribute("class");
                ret += ">";
                break;
            case REPLACED_CONTENT:
                ret = "[replaced content]";
                break;
            case TEXT_CONTENT:
                ret = "Text: " + getText();
                break;
        }
        return ret;
    }
    
}
