/**
 * BrowserCli.java
 *
 * Created on 12. 11. 2022, 19:03:38 by burgetr
 */
package cz.vutbr.fit.layout.playwright;

import java.io.IOException;
import java.io.PrintStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import cz.vutbr.fit.layout.playwright.impl.BrowserControl;

/**
 * 
 * @author burgetr
 */
public class BrowserCli
{
    
    public static void main( String[] args )
    {
        try (BrowserControl bc = new BrowserControl()) {

            bc.setNoHeadless(true);
            bc.setPersist(3);
            bc.setIncludeScreenshot(true);
            
            var pg = bc.visit("https://www.fit.vut.cz/person/burgetr");
            
            Gson gson = new Gson();
            JsonElement jsonPage = gson.toJsonTree(pg);
            
            try (PrintStream out = new PrintStream("/tmp/bbb.json")) {
                out.println(gson.toJson(jsonPage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
    }
    
}
