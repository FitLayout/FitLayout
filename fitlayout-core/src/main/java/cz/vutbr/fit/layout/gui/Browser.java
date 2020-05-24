/**
 * Browser.java
 *
 * Created on 23. 1. 2015, 13:36:15 by burgetr
 */
package cz.vutbr.fit.layout.gui;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;

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
     * Adds a page artifact from the external source
     * @param page
     */
    public void addPage(Page page);
    
    /**
     * Gets existing page models
     * @return existing page models
     */
    public List<Page> getPages();
    
    /**
     * Gets the currently selected page.
     * @return currently selected page or {@code null}.
     */
    public Page getSelectedPage();
    
    /**
     * Gets the currently selected artifact.
     * @return currently selected artifact or {@code null}.
     */
    public Artifact getSelectedArtifact();
    
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
