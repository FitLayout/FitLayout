/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.json.impl;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.json.parser.InputFile;
import cz.vutbr.fit.layout.json.parser.MetadataDef;
import cz.vutbr.fit.layout.json.parser.PageInfo;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Metadata;
import cz.vutbr.fit.layout.model.Page;

/**
 * A generic box tree builder for JSON-based renderers.
 * 
 * @author burgetr
 */
public abstract class JSONBoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(JSONBoxTreeBuilder.class);
    
    public static final String DEFAULT_FONT_FAMILY = "sans-serif";
    public static final float DEFAULT_FONT_SIZE = 12;
    
    /** Input JSON representation */
    private InputFile inputFile;
    
    /** The resulting page */
    private PageImpl page;
    

    public JSONBoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }
    
    public void parse(String urlstring) throws MalformedURLException, IOException, InterruptedException
    {
        urlstring = urlstring.trim();
        if (urlstring.startsWith("http:") ||
            urlstring.startsWith("https:") ||
            urlstring.startsWith("ftp:") ||
            urlstring.startsWith("file:"))
        {
            parse(new URL(urlstring));
        }
        else if (urlstring.startsWith("json:"))
        {
            parseJSON(urlstring.substring(5));
        }
        else
            throw new MalformedURLException("Unsupported protocol in " + urlstring);
    }
    
    public void parse(URL url) throws IOException, InterruptedException
    {
        inputFile = invokeRenderer(url);
        if (inputFile != null && inputFile.getError() == null)
        {
            if (inputFile.getStatus() == 0 //status 0 occurs for example for file: urls
                    || (inputFile.getStatus() >= 200 && inputFile.getStatus() < 300))
                parseInputFile(inputFile, url);
            else
                throw new IOException("HTTP status: " + inputFile.getStatus() + " " + inputFile.getStatusText());
        }
        else if (inputFile.getError() != null)
            throw new IOException(inputFile.getError());
        else
            throw new IOException("Backend execution failed");
    }
    
    public void parseJSON(String path) throws IOException
    {
        inputFile = loadJSON(path);
        URL url;
        try {
            if (inputFile.page != null && inputFile.page.url != null)
                url = new URL(inputFile.page.url);
            else
                url = new URL("http://url.not.available");
        } catch (MalformedURLException e) {
            url = new URL("http://url.not.available");
        }
        parseInputFile(inputFile, url);
    }
    
    protected void parseInputFile(InputFile input, URL url) throws IOException
    {
        inputFile = input;

        //create the page
        PageInfo pInfo = inputFile.getPage();
        page = new PageImpl(url);
        page.setTitle(pInfo.getTitle());
        page.setWidth(Math.round(pInfo.getWidth()));
        page.setHeight(Math.round(pInfo.getHeight()));
        if (input.getScreenshot() != null)
        {
            try {
                byte[] image = Base64.getDecoder().decode(input.getScreenshot());
                page.setPngImage(image);
            } catch (IllegalArgumentException e) {
                log.error("Couldn't decode a base64 screenshot: {}", e.getMessage());
            }
        }
        
        //create the box tree
        BoxList boxlist = new BoxList(inputFile);
        Box root = buildTree(boxlist.getVisibleBoxes(), Color.WHITE);
        page.setRoot(root);
        
        //copy the metadata (if provided)
        if (input.getMetadata() != null)
        {
            List<Metadata> data = new ArrayList<>();
            for (MetadataDef item : input.getMetadata())
                data.add(item);
            page.setMetadata(data);
        }
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }
    
    //==================================================================================
    
    /**
     * Invokes the renderer and parses its ouptut.
     * @param url
     * @return the parsed output of the backend, or {@code null} for an unexpected EOF
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract InputFile invokeRenderer(URL url) throws IOException, InterruptedException;
    
    /**
     * Parses a local file produced by the backend.
     * @param path the path to the file to parse
     * @return the parsed file
     * @throws IOException
     */
    protected InputFile loadJSON(String path) throws IOException
    {
        FileReader fin = new FileReader(path);
        Gson gson = new Gson();
        InputFile file = gson.fromJson(fin, InputFile.class);
        fin.close();
        return file;
    }
    
}
