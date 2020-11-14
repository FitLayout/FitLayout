/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

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
    
    /** Browser window width for rendering */
    private int width;
    /** Browser window height for rendering */
    private int height;
    

    public BoxTreeBuilder(int width, int height, boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
        this.width = width;
        this.height = height;
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
        parseInputFile(inputFile, url);
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
    
    /**
     * Invokes the backend and parses its ouptut.
     * @param url
     * @return the parsed output of the backend
     * @throws IOException
     * @throws InterruptedException
     */
    private InputFile invokeRenderer(URL url) throws IOException, InterruptedException
    {
        String rendererPath = System.getProperty("fitlayout.puppeteer.backend");
        if (rendererPath == null)
            throw new IOException("Puppeteer backend path is not configured. Set the fitlayout.puppeteer.backend to point to the backend installation");
        log.debug("Invoking puppeteer backend in {}", rendererPath);
        
        List<String> cmds = new ArrayList<>();
        cmds.add("node");
        cmds.add("index.js");
        cmds.add(url.toString());
        cmds.add(String.valueOf(width));
        cmds.add(String.valueOf(height));
        
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.directory(new File(rendererPath));
        Process backend = pb.start();
        
        //parse stdout by gson
        StreamConsumer outConsumer = new StreamConsumer(backend.getInputStream())
        {
            @Override
            public Object consume(InputStream stream)
            {
                Gson gson = new Gson();
                BufferedReader outReader = new BufferedReader(
                        new InputStreamReader(backend.getInputStream()));
                InputFile file = gson.fromJson(outReader, InputFile.class);
                System.err.println("Consumed ok");
                return file;
            }
        };
        var futureOut = Executors.newSingleThreadExecutor().submit(outConsumer);
                
        //collect stderr to a string
        StreamConsumer errConsumer = new StreamConsumer(backend.getErrorStream())
        {
            @Override
            public Object consume(InputStream stream)
            {
                BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(backend.getErrorStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                try {
                    while ((line = errReader.readLine()) != null)
                        builder.append(line).append(System.getProperty("line.separator"));
                } catch (IOException e) {
                    System.err.println("Err " + e);
                }
                return builder.toString();                
            }
        };
        var futureErr = Executors.newSingleThreadExecutor().submit(errConsumer);

        //wait for the backend to finish
        int exitCode = backend.waitFor();
        //wait for collectors to finish
        try {
            futureOut.get();
            futureErr.get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        
        if (exitCode != 0)
        {
            String msg = (String) errConsumer.getResult();
            throw new IOException(msg);
        }
        InputFile file = (InputFile) outConsumer.getResult();
        return file;
    }
    
    /**
     * Parses a local file produced by the backend.
     * @param path the path to the file to parse
     * @return the parsed file
     * @throws IOException
     */
    private InputFile loadJSON(String path) throws IOException
    {
        FileReader fin = new FileReader(path);
        Gson gson = new Gson();
        InputFile file = gson.fromJson(fin, InputFile.class);
        fin.close();
        return file;
    }
    
}
