/**
 * ImageInfo.java
 *
 * Created on 4. 12. 2020, 21:41:44 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.parser;

/**
 * 
 * @author burgetr
 */
public class ImageInfo
{
    public Integer id;
    public Boolean bg;
    public String data;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Boolean getBg()
    {
        return bg;
    }

    public void setBg(Boolean bg)
    {
        this.bg = bg;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }
}
