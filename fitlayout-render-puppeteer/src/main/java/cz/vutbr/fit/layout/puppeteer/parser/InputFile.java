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
    private PageInfo page;
    private String[] fonts;
    private BoxInfo[] boxes;
    

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
}
