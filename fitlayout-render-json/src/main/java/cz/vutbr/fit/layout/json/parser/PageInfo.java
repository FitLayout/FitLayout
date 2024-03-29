/**
 * PageInfo.java
 *
 * Created on 5. 11. 2020, 20:49:28 by burgetr
 */
package cz.vutbr.fit.layout.json.parser;

/**
 * 
 * @author burgetr
 */
public class PageInfo
{
    public float width;
    public float height;
    public String title;
    public String url;
    

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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
