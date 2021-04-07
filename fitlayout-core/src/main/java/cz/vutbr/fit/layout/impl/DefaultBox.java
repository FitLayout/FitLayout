/**
 * DefaultBox.java
 *
 * Created on 21. 11. 2014, 9:45:18 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Default generic box implementation.
 *  
 * @author burgetr
 */
public class DefaultBox extends DefaultTreeContentRect<Box> implements Box
{
    private int order;
    
    private boolean visible;
    private Color color;
    private String fontFamily;
    private String text;
    private ContentObject contentObject;
    
    private Rectangular contentBounds;
    private Rectangular visualBounds;
    
    private Box intrinsicParent;
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
    
    public DefaultBox(Box src)
    {
        super(Box.class, src);
        order = src.getOrder();
        visible = src.isVisible();
        color = new Color(src.getColor().getRed(), src.getColor().getGreen(), src.getColor().getBlue(), src.getColor().getAlpha());
        fontFamily = new String(src.getFontFamily());
        if (src.getOwnText() != null)
            text = new String(src.getOwnText());
        contentObject = src.getContentObject();
        contentBounds = (src.getContentBounds() == null) ? null : new Rectangular(src.getContentBounds());
        visualBounds = (src.getVisualBounds() == null) ? null : new Rectangular(src.getVisualBounds());
        sourceNodeId = src.getSourceNodeId();
        tagName = (src.getTagName() == null) ? null : new String(src.getTagName());
        if (src.getAttributes() != null)
            attributes = new HashMap<String, String>(src.getAttributes());
        type = src.getType();
        displayType = src.getDisplayType();
    }
    
    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
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
            return getOwnText();
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

    public void setOwnText(String text)
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
    
    @Override
    public void setContentBounds(Rectangular contentBounds)
    {
        this.contentBounds = contentBounds;
    }
    
    @Override
    public Rectangular getVisualBounds()
    {
        return visualBounds;
    }
    
    @Override
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

    public Box getIntrinsicParent()
    {
        return intrinsicParent;
    }

    public void setIntrinsicParent(Box intrinsicParent)
    {
        this.intrinsicParent = intrinsicParent;
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
        StringBuilder ret = new StringBuilder();
        switch (type)
        {
            case ELEMENT:
            case REPLACED_CONTENT:
                ret.append("<").append(getTagName());
                if (getAttribute("id") != null)
                    ret.append(" id=").append(getAttribute("id"));
                if (getAttribute("class") != null)
                    ret.append(" class=").append(getAttribute("class"));
                ret.append(">");
                if (type == Type.REPLACED_CONTENT)
                    ret.append(" [replaced]");
                break;
            case TEXT_CONTENT:
                ret.append("Text: ").append(getText());
                break;
        }
        return ret.toString();
    }
    
    //===========================================================================================
    
    @Override
    public boolean isVisuallySeparated()
    {
        //invisible boxes are not separated
        if (!isVisible()) 
            return false;
        //root box is visually separated
        /*else if (getParent() == null)
            return true;*/
        //non-empty text boxes are visually separated
        else if (getType() == Type.TEXT_CONTENT) 
        {
            if (getText().trim().isEmpty())
                return false;
            else
                return true;
        }
        //replaced boxes are visually separated
        else if (getType() == Type.REPLACED_CONTENT)
        {
            return true;
        }
        //list item boxes with a bullet TODO bullet detection
        else if (getDisplayType() == DisplayType.LIST_ITEM)
        {
            return true;
        }
        //other element boxes
        else 
        {
            //check if separated by border -- at least one border needed
            if (getBorderCount() >= 1)
                return true;
            //check the background
            else if (isBackgroundSeparated())
                return true;
            return false;
        }
    }
    
    @Override
    protected void recomputeTextStyle()
    {
        if (!isLeaf())
        {
            getTextStyle().reset();
            for (Box box : getChildren())
            {
                getTextStyle().updateAverages(box.getTextStyle());
            }
        }
    }
    
}
