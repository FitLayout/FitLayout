/**
 * PageSetStorage.java
 *
 * Created on 4. 2. 2016, 21:32:05 by burgetr
 */
package org.fit.layout.api;

import org.fit.layout.gui.GUIUpdateSource;

/**
 * A service that is able to manage a list of pages that are being processed.
 * It may load the next/previous page when available.
 * @author burgetr
 */
public interface PageSetStorage extends Service, GUIUpdateSource
{

    /**
     * Obtains the currently active page set that is used for storing the new pages
     * and obtaining the previous/next page.
     * @return the page set or {@code null} when no page set is active
     */
    public PageSet getCurrentPageSet();
    
    /**
     * Check whether a next page is available in the currently processed page set.
     * @return
     */
    public boolean nextPageAvailable();
    
    /**
     * Loads the next page from the current page set.
     */
    public void loadNext();
    
    /**
     * Checks whether a previous page is available in the currently processed page set.
     * @return
     */
    public boolean previousPageAvailable();
    
    /**
     * Loads the previous page from the current page set.
     */
    public void loadPrevious();
    
    /**
     * Obtains the total number of pages available in the current page set.
     * @return the page count or 0 when the page set is empty or no page set is selected.
     */
    public int getTotalCount();
    
    /**
     * Obtains the index of the currently loaded page. 
     * @return The page index or -1 when no page is loaded.
     */
    public int getCurrentIndex();
    
    /**
     * Loads the page from the storage based in its index.
     * @param index The page index.
     */
    public void loadPageAt(int index);
    
}
