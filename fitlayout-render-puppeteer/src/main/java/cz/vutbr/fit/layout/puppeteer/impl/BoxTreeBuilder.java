/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.json.impl.JSONBoxTreeBuilder;
import cz.vutbr.fit.layout.json.impl.StreamConsumer;
import cz.vutbr.fit.layout.json.parser.InputFile;

/**
 * A JSON-based builder that uses the fitlayout-puppeteer backend for obtaining the source JSON page desription.
 *  
 * @author burgetr
 */
public class BoxTreeBuilder extends JSONBoxTreeBuilder
{
    private static final String PROP_BACKEND = "fitlayout.puppeteer.backend";
    private static final String PROP_WORKDIR = "fitlayout.puppeteer.workdir";
    private static final String PROP_WRAPPER = "fitlayout.puppeteer.wrapper";

    private static Logger log = LoggerFactory.getLogger(BoxTreeBuilder.class);
    
    public static final String DEFAULT_FONT_FAMILY = "sans-serif";
    public static final float DEFAULT_FONT_SIZE = 12;
    
    /** Browser window width for rendering */
    private int width;
    /** Browser window height for rendering */
    private int height;
    /** Connection persistence */
    private int persist; 
    /** Acquire images? */
    private boolean acquireImages;
    /** Inlcude screen shots? */
    private boolean includeScreenshot;
    

    public BoxTreeBuilder(int width, int height, boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
        this.width = width;
        this.height = height;
        this.persist = 1;
        this.acquireImages = false;
        this.includeScreenshot = true;
    }
    
    public void setPersist(int persist)
    {
        this.persist = persist;
    }

    public void setAcquireImages(boolean acquireImages)
    {
        this.acquireImages = acquireImages;
    }

    public void setIncludeScreenshot(boolean includeScreenshot)
    {
        this.includeScreenshot = includeScreenshot;
    }
    
    //==================================================================================
    
    /**
     * Invokes the backend and parses its ouptut.
     * @param url
     * @return the parsed output of the backend, or {@code null} for an unexpected EOF
     * @throws IOException
     * @throws InterruptedException
     */
    protected InputFile invokeRenderer(URL url) throws IOException, InterruptedException
    {
        String rendererPath = System.getProperty(PROP_BACKEND); //puppeteer backend project path
        if (rendererPath == null)
            throw new IOException("Puppeteer backend path is not configured. Set the " + PROP_BACKEND + " to point to the backend installation");
        log.debug("Invoking puppeteer backend in {}", rendererPath);
        
        String workdir = System.getProperty(PROP_WORKDIR); //chromium working directory for storing profiles
        if (workdir != null)
            log.debug("Using chromium work directory {}", workdir);
        
        String wrapper = System.getProperty(PROP_WRAPPER); //wrapper to run the command in (e.g. xvfb-run)
        String[] wrapArgs = (wrapper == null) ? null : wrapper.split("\\s"); //split by whitespace
        
        List<String> cmds = new ArrayList<>();
        if (wrapArgs != null)
        {
            for (String arg : wrapArgs)
                cmds.add(arg);
        }
        cmds.add("node");
        cmds.add("index.js");
        cmds.add("-W" + String.valueOf(width));
        cmds.add("-H" + String.valueOf(height));
        cmds.add("-P" + String.valueOf(persist));
        if (acquireImages)
            cmds.add("-I"); //acquire images
        if (includeScreenshot)
            cmds.add("-s"); //include screenshot
        if (workdir != null)
            cmds.add("-d'" + workdir + "'");
        cmds.add(url.toString());
        if (wrapper != null)
            cmds.add("-N"); //when the wrapper is used, we don't use the headless mode
        
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
    
}
