/**
 * InputFile.java
 *
 * Created on 5. 11. 2020, 20:50:58 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.parser;

/**
 * 
 * @author burgetr
 */
public class InputFile
{
    public PageInfo page;
    public String[] fonts;
    public BoxInfo[] boxes;
    public ImageInfo[] images;
    public String screenshot;
    

    public PageInfo getPage()
    {
        return page;
    }

    public void setPage(PageInfo page)
    {
        this.page = page;
    }

    public String[] getFonts()
    {
        return fonts;
    }

    public void setFonts(String[] fonts)
    {
        this.fonts = fonts;
    }

    public BoxInfo[] getBoxes()
    {
        return boxes;
    }

    public void setBoxes(BoxInfo[] boxes)
    {
        this.boxes = boxes;
    }

    public ImageInfo[] getImages()
    {
        return images;
    }

    public void setImages(ImageInfo[] images)
    {
        this.images = images;
    }

    public String getScreenshot()
    {
        return screenshot;
    }

    public void setScreenshot(String screenshot)
    {
        this.screenshot = screenshot;
    }
}
