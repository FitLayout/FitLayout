/**
 * Browser.java
 *
 * Created on 23. 1. 2015, 13:36:15 by burgetr
 */
package org.fit.layout.gui;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.fit.layout.api.OutputDisplay;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Page;
import org.fit.layout.model.Rectangular;

/**
 * A GUI browser interface used for creating browser plugins.
 * 
 * @author burgetr
 */
public interface Browser
{

    /**
     * Adds a toolbar to the browser's toolbar area.
     * @param toolbar the toolbar to be added
     */
    public void addToolBar(JToolBar toolbar);
    
    /**
     * Adds a new tab to the tool panel at the top of the window
     * @param title the tab title
     * @param component the component to be added
     */
    public void addToolPanel(String title, JComponent component);
    
    /**
     * Adds a new tab to the structure view tabs.
     * @param title the tab title
     * @param component the component to be added
     */
    public void addStructurePanel(String title, JComponent component);
    
    /**
     * Adds a new panel to the details area.
     * @param component the component to be added
     * @param weighty the resizing vertical weight
     */
    public void addInfoPanel(JComponent component, double weighty);
    
    /**
     * Gets the browser page output display.
     * @return the output display
     */
    public OutputDisplay getOutputDisplay();
    
    /**
     * Performs the output display refresh (when something has been
     * painted).
     */
    public void updateDisplay();
    
    /**
     * Redraws the page contents. Clears the selection, highlighting etc.
     */
    public void redrawPage();
    
    /**
     * Gets the last selected visual area.
     * @return the selected visual area or {@code null} when nothing is selected
     */
    public Area getSelectedArea();
    
    /**
     * Displays the detailed information about the selected area in the GUI (e.g. a dedicated panel)
     * @param area the area to be described
     */
    public void displayAreaDetails(Area area);
    
    /**
     * Registers an area selection listener that is notified when the area selection changes.
     * @param listener the listener to be registered
     */
    public void addAreaSelectionListener(AreaSelectionListener listener);
    
    /**
     * Registers an area selection listener that is notified when some tree is updated.
     * @param listener the listener to be registered
     */
    public void addTreeListener(TreeListener listener);

    /**
     * Registers a listener for browser canvas clicks.
     * @param toggleButtonTitle The title of the toggle button that activates the listener or {@code null} for always receiving the clicks.
     * @param listener the listener to be registered
     * @param select make the button selected after adding
     */
    public void addCanvasClickListener(String toggleButtonTitle, CanvasClickListener listener, boolean select);
    
    /**
     * Registers a rectangle selection listener that is notified when a new rectangle is selected.
     * @param listener the listener to be registered
     */
    public void addRectangleSelectionListener(RectangleSelectionListener listener);
    
    /**
     * Unregisters a rectangle selection listener.
     * @param listener the listener to be unregistered
     */
    public void removeRectangleSelectionListener(RectangleSelectionListener listener);
    
    /**
     * Sets page model from the external source
     * @param page
     */
    public void setPage(Page page);
    
    /**
     * Gets the actual page model
     * @return the actual page model
     */
    public Page getPage();
    
    
    /**
     * Gets the area tree of the current page
     * @return the current area tree or {@code null} if the tree has not been built yet
     */
    public AreaTree getAreaTree();
    
    /**
     * Gets the logical area tree of the actual page
     * @return the current logical tree or {@code null} if the tree has not been built yet
     */
    public LogicalAreaTree getLogicalTree();
    
    /**
     * Sets area tree from external source
     */
    public void setAreaTree(AreaTree areaTree);
    
    /**
     * Sets logical area tree from external source
     */
    public void setLogicalTree(LogicalAreaTree logicalTree);
    
    /**
     * Updates the tree views after some trees have been changed.
     */
    public void refreshView();

    /**
     * Displays an error message dialog.
     * @param text the text do be displayed
     */
    public void displayErrorMessage(String text);
    
    /**
     * Displays an info message dialog.
     * @param text the text do be displayed
     */
    public void displayInfoMessage(String text);
    
    /**
     * Graphically selects a rectangular area in the browser window.
     * @param rect The size and position of the selected area.
     */
    public void setSelection(Rectangular rect);
    
    /**
     * Clears the graphical selection.
     */
    public void clearSelection();
    
}
