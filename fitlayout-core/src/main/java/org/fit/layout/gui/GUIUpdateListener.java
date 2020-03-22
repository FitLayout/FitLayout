/**
 * GUIUpdateListener.java
 *
 * Created on 4. 2. 2016, 23:59:23 by burgetr
 */
package org.fit.layout.gui;

/**
 * A listener that is able to update its GUI when something happens.
 * 
 * @author burgetr
 */
public interface GUIUpdateListener
{

    /**
     * Performs the GUI update according to the current conditions.
     */
    public void updateGUI();
    
}
