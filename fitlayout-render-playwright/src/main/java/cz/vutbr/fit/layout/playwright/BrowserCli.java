/**
 * BrowserCli.java
 *
 * Created on 12. 11. 2022, 19:03:38 by burgetr
 */
package cz.vutbr.fit.layout.playwright;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import cz.vutbr.fit.layout.playwright.impl.BrowserControl;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Puppetter browser CLI
 *  
 * @author burgetr
 */
@Command(name = "BrowserControl", mixinStandardHelpOptions = true, sortOptions = false, abbreviateSynopsis = true)
public class BrowserCli implements Runnable
{
    @Option(order = 1, names = {"-W", "--width"}, paramLabel = "width",
            description = "Target page width")
    protected int width = 1200;
    
    @Option(order = 2, names = {"-H", "--height"}, paramLabel = "height",
            description = "Target page height")
    protected int height = 800;
    
    @Option(order = 3, names = {"-P", "--persistence"}, paramLabel = "value",
            description = "Content downloading persistence: 0 (quick), 1 (standard), 2 (wait longer), 3 (get as much as possible)")
    protected int persistence = 1;
    
    @Option(order = 4, names = {"-s", "--screenshot"}, paramLabel = "value",
            description = "Include a screenshot in the result")
    protected boolean screenshot = false;
    
    @Option(order = 5, names = {"-I", "--download-images"}, paramLabel = "value",
            description = "Download all contained images referenced in <img> elements")
    protected boolean downloadImages = false;
    
    @Option(order = 6, names = {"-N", "--no-headless"}, paramLabel = "value",
            description = "Do not use headless mode; show the browser in foreground")
    protected boolean noHeadless = false;
    
    @Option(order = 7, names = {"-C", "--no-close"}, paramLabel = "value",
            description = "Do not close the browser after the operation")
    protected boolean noClose = false;
    
    @Option(order = 8, names = {"-d", "--user-dir"}, paramLabel = "value",
            description = "Browser profile directory to be used (default location is used when not specified)")
    protected String userDir = "";
    
    @Parameters(arity = "1", index = "0", paramLabel = "url", description = "The URL to visit")
    protected String url;
    
    @Override
    public void run()
    {
        try (BrowserControl bc = new BrowserControl()) {

            bc.setWidth(width);
            bc.setHeight(height);
            bc.setNoHeadless(noHeadless);
            bc.setPersist(persistence);
            bc.setIncludeScreenshot(screenshot);
            bc.setAcquireImages(downloadImages);
            bc.setUserDir(userDir);
            
            var pg = bc.visit(url);
            
            Gson gson = new Gson();
            JsonElement jsonPage = gson.toJsonTree(pg);
            System.out.println(gson.toJson(jsonPage));
            
            /*try (PrintStream out = new PrintStream("/tmp/out.json")) {
                out.println(gson.toJson(jsonPage));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            
            if (noClose)
            {
                try
                {
                    System.err.println("Press enter to quit");
                    var br = new BufferedReader(new InputStreamReader(System.in));
                    br.readLine();
                } catch (IOException e) {
                }
            }
        }        
        
    }
    
    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new BrowserCli()).execute(args); 
        System.exit(exitCode); 
    }

}
