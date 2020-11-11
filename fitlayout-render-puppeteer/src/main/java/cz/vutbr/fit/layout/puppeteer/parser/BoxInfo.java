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
    private int id;
    private Integer parent;
    private String tagName;
    private float x;
    private float y;
    private float width;
    private float height;
    private String css;
    private String text;
    
    
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
