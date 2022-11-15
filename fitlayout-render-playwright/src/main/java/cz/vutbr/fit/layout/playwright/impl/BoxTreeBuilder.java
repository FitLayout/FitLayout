/**
 * BoxTreeBuilder.java
 *
 * Created on 15. 11. 2022, 11:30:55 by burgetr
 */
package cz.vutbr.fit.layout.playwright.impl;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.json.impl.JSONBoxTreeBuilder;
import cz.vutbr.fit.layout.json.parser.InputFile;

/**
 * 
 * @author burgetr
 */
public class BoxTreeBuilder extends JSONBoxTreeBuilder
{
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
        // TODO
        return null;
    }
    
}
