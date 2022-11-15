/**
 * BrowserControl.java
 *
 * Created on 12. 11. 2022, 17:29:25 by burgetr
 */
package cz.vutbr.fit.layout.playwright.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;

import cz.vutbr.fit.layout.playwright.App;

/**
 * Runs the browser via Playwright and gets the rendered page description.
 *  
 * @author burgetr
 */
public class BrowserControl implements AutoCloseable
{
    private static final String[] clientScriptFiles = new String[] { 
            "/chromium/jfont-checker.js", 
            "/chromium/fonts.js", 
            "/chromium/lines.js", 
            "/chromium/jsonld.js", 
            "/chromium/export.js" 
    };

    private int width = 1200;
    private int height = 800;
    private int persist = 1;
    private boolean acquireImages = false;
    private boolean includeScreenshot = false;
    private boolean noHeadless = false;
    private String userDir = null;
    
    private Playwright playwright;
    private Browser currentBrowser;
    private BrowserContext currentContext;

    
    public BrowserControl()
    {
        playwright = Playwright.create();
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getPersist()
    {
        return persist;
    }

    public void setPersist(int persist)
    {
        this.persist = persist;
    }

    public boolean isAcquireImages()
    {
        return acquireImages;
    }

    public void setAcquireImages(boolean acquireImages)
    {
        this.acquireImages = acquireImages;
    }

    public boolean isIncludeScreenshot()
    {
        return includeScreenshot;
    }

    public void setIncludeScreenshot(boolean includeScreenshot)
    {
        this.includeScreenshot = includeScreenshot;
    }

    public boolean isNoHeadless()
    {
        return noHeadless;
    }

    public void setNoHeadless(boolean noHeadless)
    {
        this.noHeadless = noHeadless;
    }

    public String getUserDir()
    {
        return userDir;
    }

    public void setUserDir(String userDir)
    {
        this.userDir = userDir;
    }
    
    //================================================================================
    
    public Map<String, Object> visit(String urlstring)
    {
        String lastError = null;
        initCurrentContext();
        
        Page.NavigateOptions waitOptions;
        int scrollPages = 20;
        switch (persist)
        {
            case 0:
                waitOptions = new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(10000);
                scrollPages = 1;
                break;
            case 1:
                waitOptions = new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.LOAD)
                    .setTimeout(15000);
                break;
            case 2:
                waitOptions = new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(15000);
                break;
            default:
                waitOptions = new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(50000);
                break;
        }
        
        // create the page
        Page page = currentContext.newPage();
        
        // open the target page
        Response lastResponse = null;
        try {
            lastResponse = page.navigate(urlstring, waitOptions);
        } catch (Exception e) {
            lastError = e.getMessage();
        }

        // scroll the page to load more content
        String scrollFn = loadResource("/common/scroll.js");
        var totalHeight = page.evaluate(scrollFn, scrollPages);
        if (totalHeight instanceof Integer)
            page.setViewportSize(width, ((Integer) totalHeight).intValue());
        
        // take the screenshot
        byte[] screenshot = page.screenshot(
                new Page.ScreenshotOptions()
                .setFullPage(true).setType(ScreenshotType.PNG));
        
        // run the analysis script
        String clientScript = getClientScript();
        String execScript = "() => { \n" 
                + clientScript 
                + "   fitlayoutDetectLines();"
                + "   return fitlayoutExportBoxes();"
                + "}"; 
        @SuppressWarnings("unchecked")
        Map<String, Object> ret = (Map<String, Object>) page.evaluate(execScript);
        
        // add the screenshot to the page if required
        if (includeScreenshot)
        {
            ret.put("screenshot", Base64.getEncoder().encodeToString(screenshot));
        }
        
        // capture tha background images if required
        var imagesObj = ret.get("images");
        if (acquireImages && imagesObj != null && imagesObj instanceof List)
        {
            @SuppressWarnings("unchecked")
            final List<Object> images = (List<Object>) imagesObj;
            // hide the contents of the marked elemens
            page.addStyleTag(new Page.AddStyleTagOptions().setContent("[data-fitlayoutbg=\"1\"] * { display: none }"));
            // take the screenshots
            for (Object imgObj : images)
            {
                if (imgObj instanceof Map)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> img = (Map<String, Object>) imgObj;
                    boolean bg = (img.get("bg") instanceof Boolean) && ((Boolean) img.get("bg")).booleanValue();
                    String id = String.valueOf(img.get("id"));
                    String selector = "*[data-fitlayoutid=\"" + id + "\"]";
                    
                    try {
                        
                        Locator elem = page.locator(selector);
                        if (elem != null)
                        {
                            if (bg)
                            {
                                // for background images switch off the contents
                                elem.evaluate("e => { e.setAttribute('data-fitlayoutbg', '1'); }");
                            }
                            
                            var imgData = elem.screenshot(new Locator.ScreenshotOptions()
                                    .setType(ScreenshotType.PNG)
                                    .setTimeout(100));
                            img.put("data", Base64.getEncoder().encodeToString(imgData));
                            
                            if (bg)
                            {
                                // for background images switch the contents on again
                                elem.evaluate("e => { e.setAttribute('data-fitlayoutbg', '0'); }");
                            }
                        }
                        
                    } catch (Exception e) {
                        System.err.println(img + " : " + e.getMessage());
                        //e.printStackTrace();
                    }
                }
            }
        }
        
        // add the response details
        if (lastResponse != null)
        {
            ret.put("status", lastResponse.status());
            ret.put("statusText", lastResponse.statusText());
        }
        
        if (lastError != null)
        {
            ret.put("error", lastError);
        }
        
        return ret;
    }

    @Override
    public void close()
    {
        if (currentContext != null)
            currentContext.close();
        if (currentBrowser != null)
            currentBrowser.close();
        playwright.close();
    }
    
    protected BrowserType createBrowserType()
    {
        return playwright.chromium();
    }
    
    protected void initCurrentContext()
    {
        final BrowserType btype = createBrowserType();
        if (userDir == null)
        {
            // no user dir specified, create a temporary context
            currentContext = launchTemporary(btype);
        }
        else
        {
            // use the persistent context
            final Path userDirPath = Paths.get(userDir);
            currentContext = launchPersistent(btype, userDirPath);
        }
    }
    
    protected BrowserContext launchTemporary(BrowserType btype)
    {
        var opts = new BrowserType.LaunchOptions();
        opts.setHeadless(!noHeadless);
        opts.setSlowMo(100);
        opts.setChromiumSandbox(false);
        //opts.setArgs(List.of("--window-size=" + width + "x" + "height"));
        opts.setIgnoreDefaultArgs(List.of("--disable-extensions"));
        currentBrowser = btype.launch(opts);
        
        var copts = new Browser.NewContextOptions();
        copts.setScreenSize(width, height);
        copts.setViewportSize(width, height);
        return currentBrowser.newContext(copts);
    }
    
    protected BrowserContext launchPersistent(BrowserType btype, Path userDir)
    {
        var opts = new BrowserType.LaunchPersistentContextOptions();
        opts.setHeadless(!noHeadless);
        opts.setSlowMo(100);
        opts.setChromiumSandbox(false);
        //opts.setArgs(List.of("--window-size=" + width + "x" + "height"));
        opts.setIgnoreDefaultArgs(List.of("--disable-extensions"));
        opts.setScreenSize(width, height);
        opts.setViewportSize(width, height);
        return btype.launchPersistentContext(userDir, opts);
    }
    
    protected String getClientScript()
    {
        StringBuilder s = new StringBuilder();
        for (String name : clientScriptFiles)
            s.append(loadResource(name));
        return s.toString();
    }
    
    protected static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(App.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }
   
}
