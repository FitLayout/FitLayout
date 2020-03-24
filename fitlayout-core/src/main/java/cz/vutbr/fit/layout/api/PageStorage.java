/**
 * PageStorage.java
 *
 * Created on 4. 2. 2016, 21:07:11 by burgetr
 */
package cz.vutbr.fit.layout.api;

import cz.vutbr.fit.layout.gui.GUIUpdateSource;

/**
 * A service that is able to save the current page to a storage and to update
 * the page currently stored in the storage.
 * 
 * @author burgetr
 */
public interface PageStorage extends Service, GUIUpdateSource
{

    /**
     * Checks whether the save function is available (i.e. the
     * storage is ready).
     * @return {@code true} when the storage is ready to save the new page.
     */
    public boolean saveAvailable();
    
    /**
     * Saves the current page (including the area tree and the logical tree
     * if available) as the new entity in the storage. 
     */
    public void saveCurrentPage();

    /**
     * Checks whether the current page may be updated in the storage, i.e.
     * the storage is ready and the page has been already saved.
     * @return {@code true} when the page may be updated in the storage
     */
    public boolean updateAvailable();
    
    /**
     * Updates the current page in the storage.
     */
    public void updateCurrentPage();
    
}
