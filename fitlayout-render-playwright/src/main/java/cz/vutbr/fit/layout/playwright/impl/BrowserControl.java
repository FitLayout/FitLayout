/**
 * BrowserControl.java
 *
 * Created on 12. 11. 2022, 17:29:25 by burgetr
 */
package cz.vutbr.fit.layout.playwright.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
    
    public void visit(String urlstring)
    {
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
        
        Page page = currentContext.newPage();
        page.navigate(urlstring, waitOptions);

        String scrollFn = loadResource("/common/scroll.js");
        page.evaluate(scrollFn, scrollPages);
        
        String clientScript = getClientScript();
        String execScript = "() => { \n" 
                + clientScript 
                + "   fitlayoutDetectLines();"
                + "   return fitlayoutExportBoxes();"
                + "}"; 
        Object ret = page.evaluate(execScript);
        
        System.out.println(ret);
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
