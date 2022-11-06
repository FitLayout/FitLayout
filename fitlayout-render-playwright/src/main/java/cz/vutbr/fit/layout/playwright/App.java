package cz.vutbr.fit.layout.playwright;

import java.util.Scanner;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final String[] chromiumScripts = new String[] { "jfont-checker.js", "fonts.js", "lines.js", "jsonld.js", "export.js" };
    
    public void run()
    {
        try (Playwright playwright = Playwright.create()) {
            //Browser browser = playwright.chromium().launch();
            BrowserType btype = playwright.chromium();
            Browser browser = btype.launch(new BrowserType.LaunchOptions() // or firefox, webkit
                    .setHeadless(false)
                    .setSlowMo(100));

            String clientScript = getClientScript();
            String execScript = "() => { \n" 
                    + clientScript 
                    + "   fitlayoutDetectLines();"
                    + "   return fitlayoutExportBoxes();"
                    + "}"; 
            
            Page page = browser.newPage();
            page.navigate("https://fit.vut.cz");
            Object ret = page.evaluate(execScript);
            
            System.out.println(ret);
          }
    }
    
    private String getClientScript()
    {
        StringBuilder s = new StringBuilder();
        for (String name : chromiumScripts)
        {
            s.append(loadResource("/chromium/" + name));
        }
        return s.toString();
    }
    
    private static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(App.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }

    public static void main( String[] args )
    {
        App app = new App();
        app.run();
    }
}
