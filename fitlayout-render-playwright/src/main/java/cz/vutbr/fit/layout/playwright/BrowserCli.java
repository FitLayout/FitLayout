/**
 * BrowserCli.java
 *
 * Created on 12. 11. 2022, 19:03:38 by burgetr
 */
package cz.vutbr.fit.layout.playwright;

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
            
            bc.visit("https://www.fit.vut.cz/person/burgetr");
        }        
    }
    
}
