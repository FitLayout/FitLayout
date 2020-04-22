/**
 * BrowserTab.java
 *
 * Created on 22. 4. 2020, 15:48:45 by burgetr
 */
package cz.vutbr.fit.layout.tools.gui;

import javax.swing.JPanel;

/**
 * A generic browser tab interface.
 * 
 * @author burgetr
 */
public interface BrowserTab
{
    
    /**
     * The title of the tab.
     * @return the tab title
     */
    public String getTitle();
    
    /**
     * Gets the main tab panel (displayed at the top tabs)
     * 
     * @return the tabs panel
     */
    public JPanel getTabPanel();
    
    /**
     * Gets the structure panel (to be displayed in the left column)
     * 
     * @return the structure panel
     */
    public JPanel getStructurePanel();
    
    /**
     * Gets the properties panel (to be displayed in the right column)
     * 
     * @return the properties panel
     */
    public JPanel getPropertiesPanel();
    
    /**
     * Sets the status of the tab to active/inactive.
     * 
     * @param active
     */
    public void setActive(boolean active);
    
    /**
     * Reload the model (e.g. the underlying tree)
     */
    public void refreshView();
    
    /**
     * Reloads the values of the service parametres in the configuration dialogs (if any)
     * if they have been changed in the background.
     */
    public void reloadServiceParams();

}
