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
    public String tagName;
    public float x;
    public float y;
    public float width;
    public float height;
    public String css;
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

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
