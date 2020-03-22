/**
 * AreaSelectionListener.java
 *
 * Created on 23. 1. 2015, 14:28:58 by burgetr
 */
package org.fit.layout.gui;

import org.fit.layout.model.Area;

/**
 * A listener that can be called when the area selection changes in the browser.
 * 
 * @author burgetr
 */
public interface AreaSelectionListener
{
    
    /**
     * This method is called when the area selection changes in the browser.
     * @param area The new selected area.
     */
    public void areaSelected(Area area);
    
}
