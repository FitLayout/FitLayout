/**
 * BrowserPlugin.java
 *
 * Created on 23. 1. 2015, 13:49:00 by burgetr
 */
package cz.vutbr.fit.layout.gui;

/**
 * A browser plugin that extend the GUI browser functionality.
 * 
 * @author burgetr
 */
public interface BrowserPlugin
{

    /**
     * Performs the plugin initialization. This is called when the plugin is inserted to the GUI browser.
     * @param browser the browser where the plugin is used
     * @return <code>true</code> when the initialization succeeded, <code>false</code> otherwise
     */
    public boolean init(Browser browser);
    
}
