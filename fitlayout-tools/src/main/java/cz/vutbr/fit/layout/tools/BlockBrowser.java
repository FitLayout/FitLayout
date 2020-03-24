/**
 * 
 */
package cz.vutbr.fit.layout.tools;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.BrowserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeProvider;
import cz.vutbr.fit.layout.api.BoxTreeProvider;
import cz.vutbr.fit.layout.api.LogicalTreeProvider;
import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.gui.AreaSelectionListener;
import cz.vutbr.fit.layout.gui.Browser;
import cz.vutbr.fit.layout.gui.BrowserPlugin;
import cz.vutbr.fit.layout.gui.CanvasClickListener;
import cz.vutbr.fit.layout.gui.RectangleSelectionListener;
import cz.vutbr.fit.layout.gui.TreeListener;
import cz.vutbr.fit.layout.impl.DefaultContentRect;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.process.GUIProcessor;

import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import java.awt.GridBagConstraints;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.GridLayout;

import javax.swing.JTree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.GridBagLayout;

import javax.swing.JToggleButton;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;

/**
 * @author burgetr
 *
 */
public class BlockBrowser implements Browser
{
    private static Logger log = LoggerFactory.getLogger(BlockBrowser.class);
    
    public static BlockBrowser browser;

    public static final float TAG_PROBABILITY_THRESHOLD = 0.3f; 
    private static final java.awt.Color selectionColor = new java.awt.Color(127, 127, 255, 127);
    
    private BrowserConfig config;
    private GUIProcessor proc;
    private URL currentUrl = null;
    private boolean dispFinished = false;
    private boolean areasync = true;
    private boolean logsync = true;
    private boolean rectSelection = false; //rectangle area selection in progress
    private int rectX1, rectY1; //rectangle selection start point
    private Selection selection; //selection box
    private Set<String> tagTypes; //all known tag types in the area tree
    private Set<String> tagNames; //all known area names in the area tree
    
    private List<AreaSelectionListener> areaListeners;
    private List<TreeListener> treeListeners;
    private List<RectangleSelectionListener> rectangleListeners;
    private List<CanvasClickListener> canvasClickAlwaysListeners;
    private Map<JToggleButton, CanvasClickListener> canvasClickToggleListeners;

    private JFrame mainWindow = null;  //  @jve:decl-index=0:visual-constraint="-239,28"
    private JPanel container = null;
    private JPanel mainPanel = null;
    private JPanel contentPanel = null;
    private JPanel structurePanel = null;
    private JPanel statusPanel = null;
    private JTextField statusText = null;
    private JButton okButton = null;
    private JTabbedPane sidebarPane = null;
    private JPanel boxTreePanel = null;
    private JScrollPane boxTreeScroll = null;
    private JTree boxTree = null;
    private JScrollPane contentScroll = null;
    private JPanel contentCanvas = null;
    private JSplitPane mainSplitter = null;
    private JToolBar showToolBar = null;
    private JButton redrawButton = null;
	private JPanel areaTreePanel = null;
	private JScrollPane areaTreeScroll = null;
	private JTree areaJTree = null;
    private JButton showBoxButton = null;
    private JButton showAreaButton = null;
    private JToolBar lookupToolBar = null;
    private JPanel toolPanel = null;
    private JPanel logicalTreePanel = null;
    private JScrollPane logicalTreeScroll = null;
    private JTree logicalTree = null;
    private JButton refreshButton = null;
    private JSplitPane infoSplitter = null;
    private JPanel objectInfoPanel = null;
    private JScrollPane objectInfoScroll = null;
    private JTable infoTable = null;
    private JButton showArtAreaButton = null;
    private JButton showColumnsButton = null;
    private JPanel pathsPanel;
    private JScrollPane pathListScroll;
    private JScrollPane extractionScroll;
    private JTable extractionTable;
    private JFrame treeCompWindow;
    private JScrollPane probabilityScroll;
    private JTable probTable;
    private JTabbedPane toolTabs;
    private JPanel sourcesTab;
    private JLabel rendererLabel;
    private JComboBox<BoxTreeProvider> rendererCombo;
    private JPanel rendererChoicePanel;
    private JPanel rendererParamsPanel;
    private JPanel segmChoicePanel;
    private JPanel segmParamsPanel;
    private JLabel lblSegmentator;
    private JComboBox<AreaTreeProvider> segmentatorCombo;
    private JCheckBox segmAutorunCheckbox;
    private JButton segmRunButton;
    private JButton btnOperators;
    private JFrame operatorWindow;
    private JPanel logicalChoicePanel;
    private JPanel logicalParamsPanel;
    private JLabel lblLogicalBuilder;
    private JComboBox<LogicalTreeProvider> logicalCombo;
    private JButton logicalRunButton;
    private JCheckBox logicalAutorunCheckbox;


    public BlockBrowser()
    {
        config = new BrowserConfig();
        areaListeners = new LinkedList<>();
        treeListeners = new LinkedList<>();
        rectangleListeners = new LinkedList<>();
        canvasClickAlwaysListeners = new LinkedList<>();
        canvasClickToggleListeners = new HashMap<>();
        proc = new GUIProcessor() {
            @Override
            protected void treesCompleted()
            {
                refreshView();
            }
            @Override
            public void setServiceParams(String serviceName, Map<String, Object> params)
            {
                super.setServiceParams(serviceName, params);
                reloadServiceParams();
            }
        };
    }
    
    //===========================================================================
    
    public void setLocation(String url)
    {
        ((ParamsPanel) rendererParamsPanel).setParam("url", url);
        displaySelectedURL();
    }
    
    public String getLocation()
    {
        return currentUrl.toString();
    }

    public void setLoadImages(boolean b)
    {
        config.setLoadImages(b);
    }
    
    public boolean getLoadImages()
    {
        return config.getLoadImages();
    }
    
    //=============================================================================================================
    
    @Override
    public void refreshView()
    {
        boxTree.setModel(new BoxTreeModel(proc.getPage().getRoot()));
        if (proc.getAreaTree() != null)
        {
            TreePath path = areaJTree.getSelectionPath();
            areaJTree.setModel(new AreaTreeModel(proc.getAreaTree().getRoot()));
            if (path != null)
                areaJTree.setSelectionPath(path);
        }
        if (proc.getLogicalAreaTree() != null)
        {
            TreePath path = logicalTree.getSelectionPath();
            logicalTree.setModel(new LogicalTreeModel(proc.getLogicalAreaTree().getRoot()));
            if (path != null)
                logicalTree.setSelectionPath(path);
        }
    }
    
    @Override
    public void displayErrorMessage(String text)
    {
        JOptionPane.showMessageDialog(mainWindow,
                text,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void displayInfoMessage(String text)
    {
        JOptionPane.showMessageDialog(mainWindow,
                text,
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void addToolBar(JToolBar toolbar)
    {
        toolPanel.add(toolbar);
        toolPanel.updateUI();
    }

    @Override
    public void addToolPanel(String title, JComponent component)
    {
        toolTabs.addTab(title, component);
    }

    @Override
    public void addStructurePanel(String title, JComponent component)
    {
        sidebarPane.addTab(title, component);
    }

    @Override
    public void addInfoPanel(JComponent component, double weighty)
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 5, 0);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 1.0;
        constraints.weighty = weighty;
        constraints.gridx = 0;
        
        objectInfoPanel.add(component, constraints);
    }

    @Override
    public OutputDisplay getOutputDisplay()
    {
        return ((BrowserPanel) contentCanvas).getOutputDisplay();
    }

    @Override
    public void updateDisplay()
    {
        contentCanvas.repaint();
    }

    @Override
    public void redrawPage()
    {
        if (contentCanvas != null && contentCanvas instanceof BrowserPanel)
            ((BrowserPanel) contentCanvas).redrawPage();
    }

    @Override
    public Area getSelectedArea()
    {
        if (areaJTree == null)
            return null;
        else                   
            return (Area) areaJTree.getLastSelectedPathComponent();
    }

    @Override
    public void displayAreaDetails(Area area)
    {
        displayAreaInfo(area);
    }

    @Override
    public void addAreaSelectionListener(AreaSelectionListener listener)
    {
        areaListeners.add(listener);
    }
    
    @Override
    public void addTreeListener(TreeListener listener)
    {
        treeListeners.add(listener);
    }
    
    @Override
    public void addCanvasClickListener(String toggleButtonTitle, CanvasClickListener listener, boolean select)
    {
        if (toggleButtonTitle == null)
            canvasClickAlwaysListeners.add(listener);
        else
        {
            JToggleButton button = createClickToggleButton(toggleButtonTitle);
            if (select)
                button.setSelected(true); //select the first button
            canvasClickToggleListeners.put(button, listener);
            getLookupToolBar().add(button);
        }
    }
    
    @Override
    public void addRectangleSelectionListener(RectangleSelectionListener listener)
    {
        rectangleListeners.add(listener);
    }

    @Override
    public void removeRectangleSelectionListener(RectangleSelectionListener listener)
    {
        rectangleListeners.remove(listener);
    }

    @Override
	public void setPage(Page page) 
    {
    	
    	proc.setPage(page);
    	contentCanvas = createContentCanvas();
        
        contentCanvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e)
            {
                System.out.println("Click: " + e.getX() + ":" + e.getY());
                canvasClick(e.getX(), e.getY());
            }
            public void mousePressed(MouseEvent e) 
            { 
                canvasPress(e.getX(), e.getY());
            }
            public void mouseReleased(MouseEvent e)
            {
                canvasRelease(e.getX(), e.getY());
            }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) 
            {
                statusText.setText("");
            }
        });
        contentCanvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e)
            { 
                canvasDrag(e.getX(), e.getY());
            }
            public void mouseMoved(MouseEvent e) 
            { 
                String s = "Absolute: " + e.getX() + ":" + e.getY();
                Area node = (Area) areaJTree.getLastSelectedPathComponent();
                if (node != null)
                {
                    Area area = (Area) node;
                    int rx = e.getX() - area.getX1();
                    int ry = e.getY() - area.getY1();
                    s += "  Relative: " + rx + ":" + ry;
                    /*if (area.getBounds().contains(e.getX(), e.getY()))
                    {
                        AreaGrid grid = area.getGrid();
                        if (grid != null)
                        {
                            int gx = grid.findCellX(e.getX());
                            int gy = grid.findCellY(e.getY());
                            s += "  Grid: " + gx + ":" + gy;
                        }
                    }*/
                }
                statusText.setText(s);
                canvasMove(e.getX(), e.getY());
            }
        });
        contentScroll.setViewportView(contentCanvas);
        
        boxTree.setModel(new BoxTreeModel(proc.getPage().getRoot()));
        notifyBoxTreeUpdate();
        dispFinished = true;
	}

	@Override
	public Page getPage() 
	{
		return proc.getPage();
	}
    
    @Override
    public AreaTree getAreaTree()
    {
        return proc.getAreaTree();
    }

    @Override
    public LogicalAreaTree getLogicalTree()
    {
        return proc.getLogicalAreaTree();
    }

    @Override
    public void setAreaTree(AreaTree areaTree)
    {
        proc.setAreaTree(areaTree);
        updateTagLists(areaTree);
        notifyAreaTreeUpdate();
    }

    @Override
    public void setLogicalTree(LogicalAreaTree logicalTree)
    {
        proc.setLogicalAreaTree(logicalTree);
        notifyLogicalAreaTreeUpdate();
    }

    public GUIProcessor getProcessor()
    {
        return proc;
    }
    
    public void reloadServiceParams()
    {
        reloadServicePanel(rendererParamsPanel);
        reloadServicePanel(segmParamsPanel);
        reloadServicePanel(logicalParamsPanel);
    }
    
    private void reloadServicePanel(JPanel panel)
    {
        if (panel instanceof ParamsPanel)
            ((ParamsPanel) panel).reloadParams();
    }
    
    //=============================================================================================================
    
    public void displaySelectedURL()
    {
        dispFinished = false;
        if (treeCompWindow != null)
        {
            treeCompWindow.setVisible(false);
            treeCompWindow.dispose();
            treeCompWindow = null;
        }
        
        try {
            int i = rendererCombo.getSelectedIndex();
            if (i != -1)
            {
                BoxTreeProvider btp = rendererCombo.getItemAt(i);
                Page page = proc.renderPage(btp, ((ParamsPanel) rendererParamsPanel).getParams());
                setPage(page);
                
                if (segmAutorunCheckbox.isSelected())
                {
                    segmentPage();
                }
                if (logicalAutorunCheckbox.isSelected())
                {
                    buildLogicalTree();
                }
            }
        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Segments the page using the chosen provider and parametres.
     */
    private void segmentPage()
    {
        DefaultContentRect.resetId(); //reset the default ID generator to obtain the same IDs for every segmentation
        if (segmentatorCombo.getSelectedIndex() != -1)
        {
            AreaTreeProvider provider = segmentatorCombo.getItemAt(segmentatorCombo.getSelectedIndex());
            proc.segmentPage(provider, null); //the parametres should have been set through the GUI
            setAreaTree(proc.getAreaTree());
        }
    }
    
    /**
     * Builds the logical tree the chosen provider and parametres.
     */
    private void buildLogicalTree()
    {
        if (logicalCombo.getSelectedIndex() != -1)
        {
            LogicalTreeProvider provider = logicalCombo.getItemAt(logicalCombo.getSelectedIndex());
            proc.buildLogicalTree(provider, null); //the parametres should have been set through the GUI
            setLogicalTree(proc.getLogicalAreaTree());
        }
    }
    
    
    /** Creates the appropriate canvas based on the file type */
    private JPanel createContentCanvas()
    {
        if (contentCanvas != null)
        {
            contentCanvas = new BrowserPanel(proc.getPage());
            contentCanvas.setLayout(null);
            selection = new Selection();
            contentCanvas.add(selection);
            selection.setVisible(false);
            selection.setLocation(0, 0);
        }
        return contentCanvas;
    }
    
    private JToggleButton createClickToggleButton(String label)
    {
        JToggleButton button = new JToggleButton(label);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (button.isSelected())
                {
                    for (JToggleButton other : canvasClickToggleListeners.keySet())
                    {
                        if (other != button)
                            other.setSelected(false);
                    }
                }
            }
        });
        //button.setText(label);
        button.setToolTipText("Show " + label.toLowerCase() + " when the canvas is clicked");
        return button;
    }


    
    /** This is called when the browser canvas is clicked */
    private void canvasClick(int x, int y)
    {
        //always called listeners
        for (CanvasClickListener listener : canvasClickAlwaysListeners)
            listener.canvasClicked(x, y);
        //selected listener by toggle buttons
        for (JToggleButton button : canvasClickToggleListeners.keySet())
        {
            if (button.isSelected())
                canvasClickToggleListeners.get(button).canvasClicked(x, y);
        }
        
        /*if (lookupButton.isSelected())
        {
            if (proc.getAreaTree() != null)
            {
                Area node = proc.getAreaTree().getAreaAt(x, y);
                if (node != null)
                {
                    showAreaInTree(node);
                    showAreaInLogicalTree(node);
                }
            }
            //lookupButton.setSelected(false);
        }
        if (boxLookupButton.isSelected())
        {
            Box node = proc.getPage().getBoxAt(x, y);
            if (node != null)
                showBoxInTree(node);
            //boxLookupButton.setSelected(false);
        }
        if (sepLookupButton.isSelected())
        {
            showSeparatorAt(x, y);
        }
        if (extractButton.isSelected())
        {
            AreaNode node = proc.getAreaTree().getAreaAt(x, y);
            if (node != null)
            {
                proc.getExtractor().findArticleBounds(node);
                try {
                    PrintStream exs = new PrintStream(new FileOutputStream("test/extract.html"));
                    proc.getExtractor().dumpTo(exs);
                    exs.close();
                } catch (java.io.IOException e) {
                    System.err.println("Output failed: " + e.getMessage());
                }
                
                //String s = ex.getDescriptionX(node, 2);
                //System.out.println("Extracted: " + s);
            }
            extractButton.setSelected(false);
        }*/
    }
    
    private void canvasPress(int x, int y)
    {
        selection.setVisible(false);
        if (!rectangleListeners.isEmpty())
        {
            rectSelection = true;
            rectX1 = x;
            rectY1 = y;
            selection.setLocation(x, y);
            selection.setSize(0, 0);
            selection.setVisible(true);
        }
    }
    
    private void canvasRelease(int x, int y)
    {
        if (rectSelection)
        {
            rectSelection = false;
            Rectangular rect = new Rectangular(rectX1, rectY1, x, y);
            for (RectangleSelectionListener listener : rectangleListeners)
                listener.rectangleCreated(rect);
        }
    }
    
    private void canvasMove(int x, int y)
    {
    }
    
    private void canvasDrag(int x, int y)
    {
        if (rectSelection)
        {
            int x1 = Math.min(x, rectX1);
            int y1 = Math.min(y, rectY1);
            int x2 = Math.max(x, rectX1);
            int y2 = Math.max(y, rectY1);
            selection.setLocation(x1, y1);
            selection.setSize(x2 - x1, y2 - y1);
            updateDisplay();
        }
    }
    
    @Override
    public void setSelection(Rectangular rect)
    {
        selection.setLocation(rect.getX1(), rect.getY1());
        selection.setSize(rect.getWidth(), rect.getHeight());
        selection.setVisible(true);
    }

    @Override
    public void clearSelection()
    {
        selection.setSize(0, 0);
        selection.setVisible(false);
    }
    
    private void showBoxInTree(Box node)
    {
        //find the path to root
        int len = 0;
        for (Box b = node; b != null; b = b.getParent())
            len++;
        Box[] path = new Box[len];
        for (Box b = node; b != null; b = b.getParent())
            path[--len] = b;
        
        TreePath select = new TreePath(path);
        boxTree.setSelectionPath(select);
        //boxTree.expandPath(select);
        boxTree.scrollPathToVisible(new TreePath(path));
    }
    
    private void showAreaInTree(Area node)
    {
        //find the path to root
        int len = 0;
        for (Area a = node; a != null; a = a.getParent())
            len++;
        Area[] path = new Area[len];
        for (Area a = node; a != null; a = a.getParent())
            path[--len] = a;
        
        TreePath select = new TreePath(path);
        areaJTree.setSelectionPath(select);
        //areaTree.expandPath(select);
        areaJTree.scrollPathToVisible(new TreePath(path));
    }
    
    private void showAreaInLogicalTree(Area node)
    {
        if (proc.getLogicalAreaTree() != null && proc.getLogicalAreaTree().getRoot() != null)
        {
            LogicalArea lnode = proc.getLogicalAreaTree().getRoot().findArea(node);
            if (lnode != null)
            {
                //find the path to root
                int len = 0;
                for (LogicalArea a = lnode; a != null; a = a.getParent())
                    len++;
                LogicalArea[] path = new LogicalArea[len];
                for (LogicalArea a = lnode; a != null; a = a.getParent())
                    path[--len] = a;
                TreePath select = new TreePath(path);
                logicalTree.setSelectionPath(select);
                //logicalTree.expandPath(select);
                logicalTree.scrollPathToVisible(new TreePath(path));
            }
        }
    }
    
    private void showAllBoxes(Box root)
    {
        getOutputDisplay().drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAllBoxes(root.getChildAt(i));
    }
    
    public void showAreas(Area root, String name)
    {
        if (name == null || root.toString().contains(name))
            getOutputDisplay().drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAreas(root.getChildAt(i), name);
    }
    
    private void displayAreaInfo(Area area)
    {
        Vector<String> cols = infoTableData("Property", "Value");
        
        Vector<Vector <String>> vals = new Vector<Vector <String>>();
        //vals.add(infoTableData("Layout", area.getLayoutType().toString()));
        if (area.getParent() == null)
            vals.add(infoTableData("GP", "---"));
        else
            vals.add(infoTableData("GP", area.getParent().getTopology().getPosition(area).toString()));
        vals.add(infoTableData("Tags", tagProbabilityString(area.getTags())));
        //if (proc.getVisualClassifier() != null)
        //    vals.add(infoTableData("V. class", proc.getVisualClassifier().classifyArea(area)));
        //vals.add(infoTableData("Style probs", tagProbabilityString(proc.getMsa() != null ? proc.getMsa().classifyNode(area) : null)));
        //vals.add(infoTableData("Total probs", tagProbabilityString(proc.getTagPredictor() != null ? proc.getTagPredictor().getTagProbabilities(area) : null)));
        //vals.add(infoTableData("Importance", String.valueOf(area.getImportance())));
        //vals.add(infoTableData("Separated", (area.isSeparated()) ? "true" : "false"));
        //vals.add(infoTableData("Atomic", (area.isAtomic()) ? "true" : "false"));
        //vals.add(infoTableData("Indent scale", area.getTopology().getMinIndent() + " - " + area.getTopology().getMaxIndent()));
        //vals.add(infoTableData("Indent value", String.valueOf(proc.getFeatures().getIndentation(area))));
        //vals.add(infoTableData("Centered", (area.isCentered()) ? "true" : "false"));
        //vals.add(infoTableData("Coherent", (area.isCoherent()) ? "true" : "false"));
        //vals.add(infoTableData("Parent perc.", String.valueOf(area.getParentPercentage())));
        
        //vals.add(infoTableData("Name", area.getName()));
        vals.add(infoTableData("Bounds", area.getBounds().toString()));
        //vals.add(infoTableData("Content", (a.getContentBounds() == null) ? "" : a.getContentBounds().toString()));
        //vals.add(infoTableData("Level", String.valueOf(a.getLevel())));
        vals.add(infoTableData("Borders", borderString(area)));
        vals.add(infoTableData("Bg separated", (area.isBackgroundSeparated()) ? "true" : "false"));
        vals.add(infoTableData("Is hor. sep.", (area.isHorizontalSeparator()) ? "true" : "false"));
        vals.add(infoTableData("Is vert. sep.", (area.isVerticalSeparator()) ? "true" : "false"));
        vals.add(infoTableData("Avg. fsize", String.valueOf(area.getFontSize())));
        vals.add(infoTableData("Avg. fweight", String.valueOf(area.getFontWeight())));
        vals.add(infoTableData("Avg. fstyle", String.valueOf(area.getFontStyle())));
        //vals.add(infoTableData("Decl. fsize", String.valueOf(area.getDeclaredFontSize())));
        //vals.add(infoTableData("Luminosity", String.valueOf(area.getColorLuminosity())));
        //vals.add(infoTableData("Start color", colorString(a.getBoxes().firstElement().getStartColor())));
        if (area.getBoxes().size() > 0)
            vals.add(infoTableData("First box clr", colorString(area.getBoxes().get(0).getColor())));
        vals.add(infoTableData("Bg color", colorString(area.getBackgroundColor())));
        vals.add(infoTableData("Efficient bg", colorString(area.getEffectiveBackgroundColor())));
        
        //vals.add(infoTableData("Fg color", colorString(area.getBoxes().firstElement().getColor())));
        
        //markednessText.setText(String.format("%.2f", proc.getFeatures().getMarkedness(area)));

        //classification result
        displayProbabilityTable(area);
        
        /*Vector<Vector <String>> fvals = new Vector<Vector <String>>();
        FeatureVector f = proc.getFeatures().getFeatureVector(area);
        if (f != null)
        {
            Method[] methods = f.getClass().getMethods();
            for (Method m : methods)
            {
                try
                {
                    if (m.getName().startsWith("get") && !m.equals("getClass"))
                    {
                        Object ret = m.invoke(f, (Object []) null);
                        if (ret != null)
                            fvals.add(infoTableData(m.getName().substring(3), ret.toString()));
                    }
                    if (m.getName().startsWith("is"))
                    {
                        Object ret = m.invoke(f, (Object []) null);
                        if (ret != null)
                            fvals.add(infoTableData(m.getName().substring(2), ret.toString()));
                    }
                } catch (Exception e) {}
            }
        }*/
        
        DefaultTableModel tab = new DefaultTableModel(vals, cols);
        infoTable.setModel(tab);
        //DefaultTableModel ftab = new DefaultTableModel(fvals, cols);
        //featureTable.setModel(ftab);
    }
    
    private String borderString(Area a)
    {
        String bs = "";
        if (a.hasTopBorder()) bs += "^";
        if (a.hasLeftBorder()) bs += "<";
        if (a.hasRightBorder()) bs += ">";
        if (a.hasBottomBorder()) bs += "_";
        return bs;
    }
    
    private String colorString(Color color)
    {
        if (color == null)
            return "- transparent -";
        else
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    private String tagString(Map<Tag, Float> tags)
    {
        String ret = "";
        for (Map.Entry<Tag, Float> entry : tags.entrySet())
            ret += entry.getKey() + " ";
        return ret;
    }
    
    private String tagProbabilityString(Map<Tag, Float> map)
    {
        String ret = "";
        if (map != null)
        {
            for (Map.Entry<Tag, Float> entry : map.entrySet())
            {
                if (entry.getValue() > TAG_PROBABILITY_THRESHOLD)
                    ret += entry.getKey() + " (" + String.format("%1.2f", entry.getValue()) + ") "; 
            }
        }
        return ret;
    }
    
    private Vector<String> infoTableData(String prop, String value)
    {
        Vector<String> cols = new Vector<String>(2);
        cols.add(prop);
        cols.add(value);
        return cols;
    }
    
    private void displayProbabilityTable(Area area)
    {
        Vector<String> cols = new Vector<String>(tagNames);
        Collections.sort(cols);
        cols.insertElementAt("Type", 0);
        Vector<String> types = new Vector<String>(tagTypes);
        Collections.sort(types);

        Vector<Vector <String>> lines = new Vector<Vector <String>>(types.size());
        for (String type : types)
            lines.add(getProbTableLine(type, cols, area.getTags()));
        probTable.setModel(new DefaultTableModel(lines, cols));
    }
    
    private Vector<String> getProbTableLine(String type, List<String> names, Map<Tag, Float> data)
    {
        Vector<String> ret = new Vector<String>();
        boolean first = true;
        for (String name : names)
        {
            if (first)
            {
                ret.add(type);
                first = false;
            }
            else
            {
                Tag search = new DefaultTag(type, name);
                if (data.containsKey(search))
                    ret.add(String.format("%1.2f", data.get(search)));
                else
                    ret.add("");
            }
        }
        return ret;
    }
    
    private void notifyAreaSelection(Area area)
    {
        //notify area listeners
        for (AreaSelectionListener listener : areaListeners)
            listener.areaSelected(area);
    }
    
    private void notifyBoxTreeUpdate()
    {
        for (TreeListener listener : treeListeners)
            listener.pageRendered(getPage());
    }
    
    private void notifyAreaTreeUpdate()
    {
        for (TreeListener listener : treeListeners)
            listener.areaTreeUpdated(getAreaTree());
    }
    
    private void notifyLogicalAreaTreeUpdate()
    {
        for (TreeListener listener : treeListeners)
            listener.logicalAreaTreeUpdated(getLogicalTree());
    }
    
    private void showArea(Area area)
    {
        ((BrowserPanel) contentCanvas).getOutputDisplay().drawExtent(area);
        contentCanvas.repaint();
        
        //show the info table
        displayAreaInfo(area);
    }

    private void showLogicalArea(LogicalArea lnode)
    {
        boolean first = true;
        for (Area area : lnode.getAreas())
        {
            if (first)
                showArea(area);
            else
                ((BrowserPanel) contentCanvas).getOutputDisplay().drawExtent(area);
            first = false;
        }
        contentCanvas.repaint();
    }
    
    private void updateTagLists(AreaTree tree)
    {
        tagNames = new HashSet<String>();
        tagTypes = new HashSet<String>();
        recursiveUpdateTagLists(tree.getRoot());
    }
    
    private void recursiveUpdateTagLists(Area root)
    {
        for (Tag tag : root.getTags().keySet())
        {
            tagNames.add(tag.getValue());
            tagTypes.add(tag.getType());
        }
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveUpdateTagLists(root.getChildAt(i));
    }
    
    //===========================================================================
    
    public BrowserCanvas getBrowserCanvas()
    {
        return (BrowserCanvas) contentCanvas;
    }
    
    public void initPlugins()
    {
        for (BrowserPlugin plugin : ServiceManager.findBrowserPlugins())
        {
            log.info("Init plugin: {}", plugin.getClass().getName());
            plugin.init(this);
        }
    }
    
    /**
     * A place for adding custom GUI component to the main window
     */
    protected void initGUI()
    {
        //add the default selection buttons
        addCanvasClickListener("Boxes", new CanvasClickListener()
        {
            @Override
            public void canvasClicked(int x, int y)
            {
                Box node = proc.getPage().getBoxAt(x, y);
                if (node != null)
                    showBoxInTree(node);
            }
        }, false);
        addCanvasClickListener("Areas", new CanvasClickListener()
        {
            @Override
            public void canvasClicked(int x, int y)
            {
                List<Area> nodes = proc.getAreaTree().getAreasAt(x, y);
                if (!nodes.isEmpty())
                {
                    System.out.println("All: " + nodes);
                    Area node = nodes.get(nodes.size() - 1);
                    if (getSelectedArea() != null)
                    {
                        int i = nodes.indexOf(getSelectedArea());
                        if (i != -1) //already selected; try the previous one
                        {
                            if (i == 0)
                                i = nodes.size() - 1;
                            else
                                i = i - 1;
                            node = nodes.get(i);
                        }
                    }
                    System.out.println("Using: " + node);
                    showAreaInTree(node);
                    showAreaInLogicalTree(node);
                }
            }
        }, true);
    }
    
    //===========================================================================
    
    /**
     * This method initializes jFrame	
     * 	
     * @return javax.swing.JFrame	
     */
    public JFrame getMainWindow()
    {
        if (mainWindow == null)
        {
            mainWindow = new JFrame();
            mainWindow.setTitle("FITLayout Browser");
            mainWindow.setVisible(true);
            mainWindow.setBounds(new Rectangle(0, 0, 1489, 256));
            mainWindow.setMinimumSize(new Dimension(1200, 256));
            mainWindow.setContentPane(getContainer());
            mainWindow.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosing(java.awt.event.WindowEvent e)
                {
                    mainWindow.setVisible(false);
                    System.exit(0);
                }
            });
            initGUI();
        }
        return mainWindow;
    }

    private JPanel getContainer()
    {
        if (container == null)
        {
            container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(getToolPanel(), BorderLayout.NORTH);
            container.add(getMainPanel(), BorderLayout.CENTER);
        }
        return container;
    }
    
    private JPanel getMainPanel()
    {
        if (mainPanel == null)
        {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridy = -1;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.gridx = -1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.weighty = 1.0;
            gridBagConstraints11.insets = new Insets(0, 0, 5, 0);
            gridBagConstraints11.gridy = 1;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.weightx = 1.0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridwidth = 1;
            gridBagConstraints3.gridy = 2;
            mainPanel = new JPanel();
            GridBagLayout gbl_mainPanel = new GridBagLayout();
            mainPanel.setLayout(gbl_mainPanel);
            GridBagConstraints gbc_toolTabs = new GridBagConstraints();
            gbc_toolTabs.fill = GridBagConstraints.HORIZONTAL;
            gbc_toolTabs.weightx = 1.0;
            gbc_toolTabs.insets = new Insets(0, 0, 5, 0);
            gbc_toolTabs.gridx = 0;
            gbc_toolTabs.gridy = 0;
            mainPanel.add(getToolTabs(), gbc_toolTabs);
            mainPanel.add(getMainSplitter(), gridBagConstraints11);
            mainPanel.add(getStatusPanel(), gridBagConstraints3);
        }
        return mainPanel;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setRows(1);
            contentPanel = new JPanel();
            contentPanel.setLayout(gridLayout1);
            contentPanel.add(getContentScroll(), null);
        }
        return contentPanel;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getStructurePanel()
    {
        if (structurePanel == null)
        {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            structurePanel = new JPanel();
            structurePanel.setPreferredSize(new Dimension(200, 408));
            structurePanel.setLayout(gridLayout);
            structurePanel.add(getSidebarPane(), null);
        }
        return structurePanel;
    }

    /**
     * This method initializes jPanel2	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getStatusPanel()
    {
        if (statusPanel == null)
        {
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.insets = new java.awt.Insets(0,7,0,0);
            gridBagConstraints4.gridy = 2;
            statusPanel = new JPanel();
            statusPanel.setLayout(new GridBagLayout());
            statusPanel.add(getStatusText(), gridBagConstraints4);
        }
        return statusPanel;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getStatusText()
    {
        if (statusText == null)
        {
            statusText = new JTextField();
            statusText.setEditable(false);
            statusText.setText("Browser ready.");
        }
        return statusText;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOkButton()
    {
        if (okButton == null)
        {
            okButton = new JButton();
            okButton.setText("Go!");
            okButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    displaySelectedURL();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getSidebarPane()
    {
        if (sidebarPane == null)
        {
            sidebarPane = new JTabbedPane();
            sidebarPane.addTab("Area tree", null, getJPanel(), null);
            sidebarPane.addTab("Logical tree", null, getLogicalTreePanel(), null);
            sidebarPane.addTab("Box tree", null, getBoxTreePanel(), null);
            sidebarPane.addTab("Paths", null, getPathsPanel(), null);
        }
        return sidebarPane;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getBoxTreePanel()
    {
        if (boxTreePanel == null)
        {
            GridLayout gridLayout2 = new GridLayout();
            gridLayout2.setRows(1);
            boxTreePanel = new JPanel();
            boxTreePanel.setLayout(gridLayout2);
            boxTreePanel.add(getBoxTreeScroll(), null);
        }
        return boxTreePanel;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getBoxTreeScroll()
    {
        if (boxTreeScroll == null)
        {
            boxTreeScroll = new JScrollPane();
            boxTreeScroll.setViewportView(getBoxTree());
        }
        return boxTreeScroll;
    }

    /**
     * This method initializes jTree	
     * 	
     * @return javax.swing.JTree	
     */
    private JTree getBoxTree()
    {
        if (boxTree == null)
        {
            boxTree = new JTree();
            boxTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
            {
                public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                {
                    Box node = (Box) boxTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
	                    //node.drawExtent((BrowserCanvas) contentCanvas);
                        System.out.println("Node:" + node);
                        ((BrowserPanel) contentCanvas).getOutputDisplay().drawExtent(node);
                        contentCanvas.repaint();
                        //boxTree.scrollPathToVisible(new TreePath(node.getPath()));
                    }
                }
            });
        }
        return boxTree;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getContentScroll()
    {
        if (contentScroll == null)
        {
            contentScroll = new JScrollPane();
            contentScroll.setViewportView(getContentCanvas());
            contentScroll.getVerticalScrollBar().setUnitIncrement(10);
            contentScroll.addComponentListener(new java.awt.event.ComponentAdapter()
            {
                /*public void componentResized(java.awt.event.ComponentEvent e)
                {
                    if (contentCanvas != null && contentCanvas instanceof BrowserCanvas)
                    {
                        ((BrowserCanvas) contentCanvas).createLayout(contentScroll.getSize());
                        contentScroll.repaint();
                        BoxTree btree = new BoxTree(((BrowserCanvas) contentCanvas).getViewport());
                        boxTree.setModel(new DefaultTreeModel(btree.getRoot()));
                    }
                }*/
            });
        }
        return contentScroll;
    }

    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getContentCanvas()
    {
        if (contentCanvas == null)
        {
            contentCanvas = new JPanel();
            //contentCanvas.add(getInfoSplitter(), null);
        }
        return contentCanvas;
    }
    
    /**
     * This method initializes jSplitPane	
     * 	
     * @return javax.swing.JSplitPane	
     */
    private JSplitPane getMainSplitter()
    {
        if (mainSplitter == null)
        {
            mainSplitter = new JSplitPane();
            mainSplitter.setDividerLocation(250);
            mainSplitter.setLeftComponent(getStructurePanel());
            mainSplitter.setRightComponent(getInfoSplitter());
        }
        return mainSplitter;
    }

    /**
     * This method initializes jToolBar 
     *  
     * @return javax.swing.JToolBar 
     */
    private JToolBar getShowToolBar()
    {
        if (showToolBar == null)
        {
            showToolBar = new JToolBar();
            showToolBar.add(getRedrawButton());
            showToolBar.add(getRefreshButton());
            showToolBar.add(getShowBoxButton());
            showToolBar.add(getShowAreaButton());
            showToolBar.add(getShowArtAreaButton());
            showToolBar.add(getShowColumnsButton());
        }
        return showToolBar;
    }
    
    
    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getRedrawButton()
    {
        if (redrawButton == null)
        {
            redrawButton = new JButton();
            redrawButton.setText("Clear");
            redrawButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            redrawButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    redrawPage();
                    updateDisplay();
                }
            });
        }
        return redrawButton;
    }

    /**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel()
	{
		if (areaTreePanel == null)
		{
			GridLayout gridLayout4 = new GridLayout();
			gridLayout4.setRows(1);
			gridLayout4.setColumns(1);
			areaTreePanel = new JPanel();
			areaTreePanel.setLayout(gridLayout4);
			areaTreePanel.add(getJScrollPane(), null);
		}
		return areaTreePanel;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane()
	{
		if (areaTreeScroll == null)
		{
			areaTreeScroll = new JScrollPane();
			areaTreeScroll.setViewportView(getAreaJTree());
		}
		return areaTreeScroll;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getAreaJTree()
	{
		if (areaJTree == null)
		{
			areaJTree = new JTree();
			areaJTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
			{
				public void valueChanged(javax.swing.event.TreeSelectionEvent e)
				{
                    if (logsync)
                    {
	                    Area node = (Area) areaJTree.getLastSelectedPathComponent();
	                    if (node != null)
	                    {
	                        showArea(node);
                        	areasync = false;
                        	showAreaInLogicalTree(node);
                        	areasync = true;
	                    }
	                    notifyAreaSelection(node);
                    }
				}
			});
			areaJTree.setModel(new AreaTreeModel(null));
		}
		return areaJTree;
	}

    /**
     * This method initializes showBoxButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getShowBoxButton()
    {
        if (showBoxButton == null)
        {
            showBoxButton = new JButton();
            showBoxButton.setText("Show boxes");
            showBoxButton.setToolTipText("Show all boxes in the selected tree");
            showBoxButton.addActionListener(new java.awt.event.ActionListener()
            {
				public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    Box node = (Box) boxTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
                        showAllBoxes(node);
                        contentCanvas.repaint();
                    }
                }
            });
        }
        return showBoxButton;
    }

    /**
     * This method initializes showAreaButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getShowAreaButton()
    {
        if (showAreaButton == null)
        {
            showAreaButton = new JButton();
            showAreaButton.setText("Show areas");
            showAreaButton.setToolTipText("Show all the areas in the selected area");
            showAreaButton.addActionListener(new java.awt.event.ActionListener()
            {
				public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    Area node = (Area) areaJTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
                        showAreas(node, null);
                        contentCanvas.repaint();
                    }
                }
            });
        }
        return showAreaButton;
    }

    /**
     * This method initializes lookupToolBar	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getLookupToolBar()
    {
        if (lookupToolBar == null)
        {
            lookupToolBar = new JToolBar();
            //the default buttons are added in initGUI()
        }
        return lookupToolBar;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getToolPanel()
    {
        if (toolPanel == null)
        {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            toolPanel = new JPanel();
            //toolPanel.setLayout(new WrappingLayout(WrappingLayout.LEFT, 1, 1));
            toolPanel.setLayout(new ToolbarLayout());
            toolPanel.add(getShowToolBar());
            toolPanel.add(getLookupToolBar());
        }
        return toolPanel;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getLogicalTreePanel()
    {
        if (logicalTreePanel == null)
        {
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.fill = GridBagConstraints.BOTH;
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.weightx = 1.0;
            gridBagConstraints8.weighty = 1.0;
            gridBagConstraints8.gridx = 0;
            logicalTreePanel = new JPanel();
            logicalTreePanel.setLayout(new GridBagLayout());
            logicalTreePanel.add(getLogicalTreeScroll(), gridBagConstraints8);
        }
        return logicalTreePanel;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getLogicalTreeScroll()
    {
        if (logicalTreeScroll == null)
        {
            logicalTreeScroll = new JScrollPane();
            logicalTreeScroll.setViewportView(getLogicalJTree());
        }
        return logicalTreeScroll;
    }

    /**
     * This method initializes logicalTree	
     * 	
     * @return javax.swing.JTree	
     */
    private JTree getLogicalJTree()
    {
        if (logicalTree == null)
        {
            logicalTree = new JTree();
            logicalTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
                    {
                        public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                        {
                        	if (areasync)
                        	{
	                            LogicalArea node = (LogicalArea) logicalTree.getLastSelectedPathComponent();
	                            if (node != null)
	                            {
	                            	showLogicalArea((LogicalArea) node);
                            		logsync = false;
                            		showAreaInTree(((LogicalArea) node).getFirstArea());
                            		logsync = true;
	                            }
                        	}
                        }
                    });
            logicalTree.setModel(new LogicalTreeModel(null));
        }
        return logicalTree;
    }

    /**
     * This method initializes refreshButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getRefreshButton()
    {
        if (refreshButton == null)
        {
            refreshButton = new JButton();
            refreshButton.setText("Refresh");
            refreshButton.setToolTipText("Refresh the tree views");
            refreshButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    refreshView();
                }
            });
        }
        return refreshButton;
    }

    /**
     * This method initializes infoSplitter	
     * 	
     * @return javax.swing.JSplitPane	
     */
    private JSplitPane getInfoSplitter()
    {
        if (infoSplitter == null)
        {
            infoSplitter = new JSplitPane();
            infoSplitter.setResizeWeight(1.0);
            infoSplitter.setDividerLocation(1050);
            infoSplitter.setLeftComponent(getContentPanel());
            infoSplitter.setRightComponent(getObjectInfoPanel());
        }
        return infoSplitter;
    }

    /**
     * This method initializes objectInfoPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getObjectInfoPanel()
    {
        if (objectInfoPanel == null)
        {
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.insets = new Insets(0, 0, 5, 0);
            gridBagConstraints10.fill = GridBagConstraints.BOTH;
            gridBagConstraints10.gridy = 0;
            gridBagConstraints10.weightx = 1.0;
            gridBagConstraints10.weighty = 0.5;
            gridBagConstraints10.gridx = 0;
            objectInfoPanel = new JPanel();
            GridBagLayout gbl_objectInfoPanel = new GridBagLayout();
            gbl_objectInfoPanel.rowWeights = new double[]{0.0, 0.0};
            gbl_objectInfoPanel.columnWeights = new double[]{1.0};
            objectInfoPanel.setLayout(gbl_objectInfoPanel);
            objectInfoPanel.add(getJScrollPane4(), gridBagConstraints10);
            GridBagConstraints gbc_probabilityScroll = new GridBagConstraints();
            gbc_probabilityScroll.weighty = 0.25;
            gbc_probabilityScroll.weightx = 1.0;
            gbc_probabilityScroll.insets = new Insets(0, 0, 5, 0);
            gbc_probabilityScroll.fill = GridBagConstraints.BOTH;
            gbc_probabilityScroll.gridx = 0;
            gbc_probabilityScroll.gridy = 1;
            objectInfoPanel.add(getProbabilityScroll(), gbc_probabilityScroll);
        }
        return objectInfoPanel;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane4()
    {
        if (objectInfoScroll == null)
        {
            objectInfoScroll = new JScrollPane();
            objectInfoScroll.setViewportView(getInfoTable());
        }
        return objectInfoScroll;
    }

    /**
     * This method initializes infoTable	
     * 	
     * @return javax.swing.JTable	
     */
    private JTable getInfoTable()
    {
        if (infoTable == null)
        {
            infoTable = new JTable();
        }
        return infoTable;
    }
    
    /**
     * This method initializes showArtAreaButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getShowArtAreaButton()
    {
        if (showArtAreaButton == null)
        {
            showArtAreaButton = new JButton();
            showArtAreaButton.setText("Art. areas");
            showArtAreaButton.setToolTipText("Show artificial areas marked with <area>");       
            showArtAreaButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    Area node = (Area) areaJTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
                        showAreas(node, "<area");
                        contentCanvas.repaint();
                    }
                }
            });
        }
        return showArtAreaButton;
    }

    /**
     * This method initializes showColumnsButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getShowColumnsButton()
    {
        if (showColumnsButton == null)
        {
          showColumnsButton = new JButton();
          showColumnsButton.setText("Chunks");
          showColumnsButton.setToolTipText("Show chunk areas marked with <chunk:*>");
          showColumnsButton.addActionListener(new java.awt.event.ActionListener()
          {
              public void actionPerformed(java.awt.event.ActionEvent e)
              {
                  Area node = (Area) areaJTree.getLastSelectedPathComponent();
                  if (node != null)
                  {
                      showAreas(node, "<chunk");
                      contentCanvas.repaint();
                  }
              }
          });
        }
        return showColumnsButton;
    }

    private JPanel getPathsPanel()
    {
        if (pathsPanel == null)
        {
            pathsPanel = new JPanel();
            GridBagLayout gbl_pathsPanel = new GridBagLayout();
            gbl_pathsPanel.columnWidths = new int[] { 0, 0 };
            gbl_pathsPanel.rowHeights = new int[] { 0, 0, 0 };
            gbl_pathsPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
            gbl_pathsPanel.rowWeights = new double[] { 1.0, 1.0,
                    Double.MIN_VALUE };
            pathsPanel.setLayout(gbl_pathsPanel);
            GridBagConstraints gbc_pathListScroll = new GridBagConstraints();
            gbc_pathListScroll.insets = new Insets(0, 0, 5, 0);
            gbc_pathListScroll.fill = GridBagConstraints.BOTH;
            gbc_pathListScroll.gridx = 0;
            gbc_pathListScroll.gridy = 0;
            pathsPanel.add(getPathListScroll(), gbc_pathListScroll);
            GridBagConstraints gbc_extractionScroll = new GridBagConstraints();
            gbc_extractionScroll.fill = GridBagConstraints.BOTH;
            gbc_extractionScroll.gridx = 0;
            gbc_extractionScroll.gridy = 1;
            pathsPanel.add(getExtractionScroll(), gbc_extractionScroll);
        }
        return pathsPanel;
    }

    private JScrollPane getPathListScroll()
    {
        if (pathListScroll == null)
        {
            pathListScroll = new JScrollPane();
        }
        return pathListScroll;
    }

    private JScrollPane getExtractionScroll()
    {
        if (extractionScroll == null)
        {
            extractionScroll = new JScrollPane();
            extractionScroll.setViewportView(getExtractionTable());
        }
        return extractionScroll;
    }

    private JScrollPane getProbabilityScroll()
    {
        if (probabilityScroll == null)
        {
            probabilityScroll = new JScrollPane();
            probabilityScroll.setViewportView(getProbTable());
        }
        return probabilityScroll;
    }

    private JTable getProbTable()
    {
        if (probTable == null)
        {
            probTable = new JTable();
            probTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer()
            {
                private static final long serialVersionUID = 1L;
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
                {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    try {
                        Double d = Double.parseDouble(value.toString().replace(',', '.'));
                        if (d <= TAG_PROBABILITY_THRESHOLD)
                            c.setForeground(new java.awt.Color(180, 180, 180));
                        else
                            c.setForeground(java.awt.Color.BLACK);
                    } catch (NumberFormatException e) {
                        c.setForeground(java.awt.Color.BLACK);
                    }
                    return c;
                }
            });
        }
        return probTable;
    }

    private JTable getExtractionTable()
    {
        if (extractionTable == null)
        {
            extractionTable = new JTable();
        }
        return extractionTable;
    }
    

    private JTabbedPane getToolTabs()
    {
        if (toolTabs == null)
        {
            toolTabs = new JTabbedPane(JTabbedPane.TOP);
            toolTabs.addTab("Sources", null, getSourcesTab(), null);
        }
        return toolTabs;
    }

    private JPanel getSourcesTab()
    {
        if (sourcesTab == null)
        {
            sourcesTab = new JPanel();
            GridBagLayout gbl_sourcesTab = new GridBagLayout();
            gbl_sourcesTab.columnWeights = new double[] { 1.0 };
            gbl_sourcesTab.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 1.0 };
            sourcesTab.setLayout(gbl_sourcesTab);
            GridBagConstraints gbc_rendererChoicePanel = new GridBagConstraints();
            gbc_rendererChoicePanel.weightx = 1.0;
            gbc_rendererChoicePanel.anchor = GridBagConstraints.EAST;
            gbc_rendererChoicePanel.fill = GridBagConstraints.BOTH;
            gbc_rendererChoicePanel.insets = new Insets(0, 0, 1, 0);
            gbc_rendererChoicePanel.gridx = 0;
            gbc_rendererChoicePanel.gridy = 0;
            sourcesTab.add(getRendererChoicePanel(), gbc_rendererChoicePanel);
            GridBagConstraints gbc_rendererParamsPanel = new GridBagConstraints();
            gbc_rendererParamsPanel.weightx = 1.0;
            gbc_rendererParamsPanel.fill = GridBagConstraints.BOTH;
            gbc_rendererParamsPanel.insets = new Insets(0, 0, 2, 0);
            gbc_rendererParamsPanel.gridx = 0;
            gbc_rendererParamsPanel.gridy = 1;
            sourcesTab.add(getRendererParamsPanel(), gbc_rendererParamsPanel);
            GridBagConstraints gbc_segmChoicePanel = new GridBagConstraints();
            gbc_segmChoicePanel.weightx = 1.0;
            gbc_segmChoicePanel.anchor = GridBagConstraints.EAST;
            gbc_segmChoicePanel.fill = GridBagConstraints.BOTH;
            gbc_segmChoicePanel.insets = new Insets(0, 0, 1, 0);
            gbc_segmChoicePanel.gridx = 0;
            gbc_segmChoicePanel.gridy = 2;
            sourcesTab.add(getSegmChoicePanel(), gbc_segmChoicePanel);
            GridBagConstraints gbc_segmParamsPanel = new GridBagConstraints();
            gbc_segmParamsPanel.insets = new Insets(0, 0, 2, 0);
            gbc_segmParamsPanel.weightx = 1.0;
            gbc_segmParamsPanel.fill = GridBagConstraints.BOTH;
            gbc_segmParamsPanel.gridx = 0;
            gbc_segmParamsPanel.gridy = 3;
            sourcesTab.add(getSegmParamsPanel(), gbc_segmParamsPanel);
            GridBagConstraints gbc_logicalChoicePanel = new GridBagConstraints();
            gbc_logicalChoicePanel.insets = new Insets(0, 0, 1, 0);
            gbc_logicalChoicePanel.fill = GridBagConstraints.BOTH;
            gbc_logicalChoicePanel.gridx = 0;
            gbc_logicalChoicePanel.gridy = 4;
            sourcesTab.add(getLogicalChoicePanel(), gbc_logicalChoicePanel);
            GridBagConstraints gbc_logicalParamsPanel = new GridBagConstraints();
            gbc_logicalParamsPanel.fill = GridBagConstraints.BOTH;
            gbc_logicalParamsPanel.gridx = 0;
            gbc_logicalParamsPanel.gridy = 5;
            sourcesTab.add(getLogicalParamsPanel(), gbc_logicalParamsPanel);

            BoxTreeProvider p = (BoxTreeProvider) rendererCombo.getSelectedItem();
            if (p != null)
                ((ParamsPanel) rendererParamsPanel).setOperation(p, null);
            AreaTreeProvider ap = (AreaTreeProvider) segmentatorCombo.getSelectedItem();
            if (ap != null)
                ((ParamsPanel) segmParamsPanel).setOperation(ap, null);
            LogicalTreeProvider lp = (LogicalTreeProvider) logicalCombo.getSelectedItem();
            if (lp != null)
                ((ParamsPanel) logicalParamsPanel).setOperation(lp, null);
            
        }
        return sourcesTab;
    }

    private JLabel getRendererLabel()
    {
        if (rendererLabel == null)
        {
            rendererLabel = new JLabel("Renderer");
        }
        return rendererLabel;
    }

    protected JComboBox<BoxTreeProvider> getRendererCombo()
    {
        if (rendererCombo == null)
        {
            rendererCombo = new JComboBox<BoxTreeProvider>();
            rendererCombo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    BoxTreeProvider p = (BoxTreeProvider) rendererCombo.getSelectedItem();
                    if (p != null)
                        ((ParamsPanel) rendererParamsPanel).setOperation(p, null);
                }
            });
            Vector<BoxTreeProvider> providers = new Vector<BoxTreeProvider>(proc.getBoxProviders().values());
            DefaultComboBoxModel<BoxTreeProvider> model = new DefaultComboBoxModel<BoxTreeProvider>(providers);
            rendererCombo.setModel(model);
        }
        return rendererCombo;
    }

    private JPanel getRendererChoicePanel()
    {
        if (rendererChoicePanel == null)
        {
            rendererChoicePanel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) rendererChoicePanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            rendererChoicePanel.add(getRendererLabel());
            rendererChoicePanel.add(getRendererCombo());
            rendererChoicePanel.add(getOkButton());
        }
        return rendererChoicePanel;
    }

    private JPanel getRendererParamsPanel()
    {
        if (rendererParamsPanel == null)
        {
            rendererParamsPanel = new ParamsPanel();
        }
        return rendererParamsPanel;
    }

    private JPanel getSegmChoicePanel()
    {
        if (segmChoicePanel == null)
        {
            segmChoicePanel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) segmChoicePanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            segmChoicePanel.add(getLblSegmentator());
            segmChoicePanel.add(getSegmentatorCombo());
            segmChoicePanel.add(getSegmRunButton());
            segmChoicePanel.add(getSegmAutorunCheckbox());
            segmChoicePanel.add(getBtnOperators());
        }
        return segmChoicePanel;
    }

    private JPanel getSegmParamsPanel()
    {
        if (segmParamsPanel == null)
        {
            segmParamsPanel = new ParamsPanel();
        }
        return segmParamsPanel;
    }

    private JLabel getLblSegmentator()
    {
        if (lblSegmentator == null)
        {
            lblSegmentator = new JLabel("Segmentator");
        }
        return lblSegmentator;
    }

    protected JComboBox<AreaTreeProvider> getSegmentatorCombo()
    {
        if (segmentatorCombo == null)
        {
            segmentatorCombo = new JComboBox<AreaTreeProvider>();
            segmentatorCombo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    AreaTreeProvider ap = (AreaTreeProvider) segmentatorCombo.getSelectedItem();
                    if (ap != null)
                        ((ParamsPanel) segmParamsPanel).setOperation(ap, null);
                }
            });
            Vector<AreaTreeProvider> providers = new Vector<AreaTreeProvider>(proc.getAreaProviders().values());
            DefaultComboBoxModel<AreaTreeProvider> model = new DefaultComboBoxModel<AreaTreeProvider>(providers);
            segmentatorCombo.setModel(model);
        }
        return segmentatorCombo;
    }

    private JButton getSegmRunButton()
    {
        if (segmRunButton == null)
        {
            segmRunButton = new JButton("Run");
            segmRunButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    segmentPage();
                }
            });
        }
        return segmRunButton;
    }
    
    protected JCheckBox getSegmAutorunCheckbox()
    {
        if (segmAutorunCheckbox == null)
        {
            segmAutorunCheckbox = new JCheckBox("Run automatically");
        }
        return segmAutorunCheckbox;
    }
    
    private JButton getBtnOperators()
    {
        if (btnOperators == null)
        {
            btnOperators = new JButton("Operators...");
            btnOperators.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (operatorWindow == null)
                        operatorWindow = new OperatorConfigWindow(proc);
                    operatorWindow.pack();
                    operatorWindow.setVisible(true);
                }
            });
        }
        return btnOperators;
    }
    
    private JPanel getLogicalChoicePanel()
    {
        if (logicalChoicePanel == null)
        {
            logicalChoicePanel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) logicalChoicePanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            logicalChoicePanel.add(getLblLogicalBuilder());
            logicalChoicePanel.add(getLogicalCombo());
            logicalChoicePanel.add(getLogicalRunButton());
            logicalChoicePanel.add(getLogicalAutorunCheckbox());
            if (getLogicalCombo().getModel().getSize() == 0)
                logicalChoicePanel.setVisible(false);
        }
        return logicalChoicePanel;
    }

    private JPanel getLogicalParamsPanel()
    {
        if (logicalParamsPanel == null)
        {
            logicalParamsPanel = new ParamsPanel();
        }
        return logicalParamsPanel;
    }

    private JLabel getLblLogicalBuilder()
    {
        if (lblLogicalBuilder == null)
        {
            lblLogicalBuilder = new JLabel("Logical builder");
        }
        return lblLogicalBuilder;
    }

    protected JComboBox<LogicalTreeProvider> getLogicalCombo()
    {
        if (logicalCombo == null)
        {
            logicalCombo = new JComboBox<LogicalTreeProvider>();
            logicalCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    LogicalTreeProvider lp = (LogicalTreeProvider) logicalCombo.getSelectedItem();
                    if (lp != null)
                        ((ParamsPanel) logicalParamsPanel).setOperation(lp, null);
                }
            });
            Vector<LogicalTreeProvider> providers = new Vector<LogicalTreeProvider>(proc.getLogicalProviders().values());
            DefaultComboBoxModel<LogicalTreeProvider> model = new DefaultComboBoxModel<LogicalTreeProvider>(providers);
            logicalCombo.setModel(model);
        }
        return logicalCombo;
    }

    private JButton getLogicalRunButton()
    {
        if (logicalRunButton == null)
        {
            logicalRunButton = new JButton("Run");
            logicalRunButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    buildLogicalTree();
                }
            });
        }
        return logicalRunButton;
    }

    protected JCheckBox getLogicalAutorunCheckbox()
    {
        if (logicalAutorunCheckbox == null)
        {
            logicalAutorunCheckbox = new JCheckBox("Run automatically");
        }
        return logicalAutorunCheckbox;
    }

    private class Selection extends JPanel
    {
        private static final long serialVersionUID = 1L;
        public void paintComponent(Graphics g)
        {
            //super.paintComponent(g);
            g.setColor(selectionColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    public static void main(String[] args)
    {
        final String urlstring = (args.length > 0) ? args[0] : "http://cssbox.sf.net";
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    browser = new BlockBrowser();
                    browser.setLoadImages(false);
                    JFrame main = browser.getMainWindow();
                    //main.setSize(1000,600);
                    //main.setMinimumSize(new Dimension(1200, 600));
                    //main.setSize(1500,600);
                    main.setSize(1600,1000);
                    browser.initPlugins();
                    main.setVisible(true);
                    
                    //URL url = new URL("http://www.reuters.com/article/2014/03/28/us-trading-momentum-analysis-idUSBREA2R09M20140328");
                    URL url = new URL(urlstring);
                    browser.setLocation(url.toString());
                        
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
    }

}
