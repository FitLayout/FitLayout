/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.puppeteer.parser.InputFile;
import cz.vutbr.fit.layout.puppeteer.parser.PageInfo;

/**
 * 
 * @author burgetr
 */
public class BoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(BoxTreeBuilder.class);
    
    public static final String DEFAULT_FONT_FAMILY = "sans-serif";
    public static final float DEFAULT_FONT_SIZE = 12;
    
    /** Input JSON representation */
    private InputFile inputFile;
    
    /** The resulting page */
    private PageImpl page;
    

    public BoxTreeBuilder(int width, int height, boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }
    
    public void parse(String urlstring) throws MalformedURLException, IOException
    {
        urlstring = urlstring.trim();
        if (urlstring.startsWith("http:") ||
            urlstring.startsWith("https:") ||
            urlstring.startsWith("ftp:") ||
            urlstring.startsWith("file:"))
        {
            parse(new URL(urlstring));
        }
        else
            throw new MalformedURLException("Unsupported protocol in " + urlstring);
    }
    
    public void parse(URL url) throws IOException
    {
        //get the page data from the backend
        inputFile = invokeRenderer(url);

        //create the page
        PageInfo pInfo = inputFile.getPage();
        page = new PageImpl(url);
        page.setTitle(pInfo.getTitle());
        page.setWidth(Math.round(pInfo.getWidth()));
        page.setHeight(Math.round(pInfo.getHeight()));
        
        //create the box tree
        BoxList boxlist = new BoxList(inputFile);
        Box root = buildTree(boxlist.getVisibleBoxes(), Color.WHITE);
        page.setRoot(root);
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }
    
    //==================================================================================
    
    private InputFile invokeRenderer(URL url) throws IOException
    {
        //TODO this is a temporary stub
        FileReader fin = new FileReader(System.getProperty("user.home") + "/tmp/fitlayout/boxes.json");
        Gson gson = new Gson();
        InputFile file = gson.fromJson(fin, InputFile.class);
        fin.close();
        return file;
    }
    
}
