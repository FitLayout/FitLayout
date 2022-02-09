/**
 * BoxInfo.java
 *
 * Created on 5. 11. 2020, 20:47:21 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.parser;

/**
 * 
 * @author burgetr
 */
public class BoxInfo
{
    public int id;
    public Integer parent; //offsetParent ID reference
    public Integer domParent; //DOM parentElement ID refrence
    public String xpath;
    public String tagName;
    public Attribute[] attrs;
    public Boolean replaced;
    public float x;
    public float y;
    public float width;
    public float height;
    public String css;
    public String decoration;
    public String text;
    
    
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }

    public Integer getParent()
    {
        return parent;
    }

    public void setParent(Integer parent)
    {
        this.parent = parent;
    }

    public String getXpath()
    {
        return xpath;
    }

    public void setXpath(String xpath)
    {
        this.xpath = xpath;
    }

    public String getTagName()
    {
        return tagName;
    }

    public Integer getDomParent()
    {
        return domParent;
    }

    public void setDomParent(Integer domParent)
    {
        this.domParent = domParent;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public Attribute[] getAttrs()
    {
        return attrs;
    }

    public void setAttrs(Attribute[] attrs)
    {
        this.attrs = attrs;
    }

    public Boolean getReplaced()
    {
        return replaced;
    }

    public void setReplaced(Boolean replaced)
    {
        this.replaced = replaced;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public float getWidth()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }

    public float getHeight()
    {
        return height;
    }

    public void setHeight(float height)
    {
        this.height = height;
    }

    public String getCss()
    {
        return css;
    }

    public void setCss(String css)
    {
        this.css = css;
    }

    public String getDecoration()
    {
        return decoration;
    }

    public void setDecoration(String decoration)
    {
        this.decoration = decoration;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
