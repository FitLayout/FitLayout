/**
 * BoxTreeTab.java
 *
 * Created on 21. 4. 2020, 23:40:03 by burgetr
 */
package cz.vutbr.fit.layout.tools.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import cz.vutbr.fit.layout.gui.CanvasClickListener;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.tools.BlockBrowser;
import cz.vutbr.fit.layout.tools.BoxTreeModel;

/**
 * A composition of the box sources panel and the corresponding gui parts.
 * 
 * @author burgetr
 */
public class BoxTreeTab extends BrowserTabBase implements CanvasClickListener
{
    private BoxSourcePanel boxSourcePanel;
    private JPanel structurePanel;
    private JPanel propertiesPanel;
    private JTree boxTree;
    private JTable infoTable;
    
    
    public BoxTreeTab(BlockBrowser browser)
    {
        super(browser);
        boxSourcePanel = new BoxSourcePanel(browser);
        structurePanel = createStructurePanel();
        propertiesPanel = createPropertiesPanel();
        
        browser.addCanvasClickListener(null, this, false);

    }
    
    @Override
    public String getTitle()
    {
        return "Box tree";
    }

    @Override
    public JPanel getStructurePanel()
    {
        return structurePanel;
    }

    @Override
    public JPanel getPropertiesPanel()
    {
        return propertiesPanel;
    }

    @Override
    public JPanel getTabPanel()
    {
        return boxSourcePanel;
    }
    
    @Override
    public void reloadServiceParams()
    {
        boxSourcePanel.reloadServiceParams();
    }
    
    @Override
    public void refreshView()
    {
        //reloads the box tree from the processor
        boxTree.setModel(new BoxTreeModel(browser.getProcessor().getPage().getRoot()));
    }

    @Override
    public void canvasClicked(int x, int y)
    {
        if (isActive())
        {
            Box node = browser.getPage().getBoxAt(x, y);
            if (node != null)
                showBoxInTree(node);
        }
    }

    //=================================================================================================
    
    public Box getSelectedBox()
    {
        if (boxTree == null)
            return null;
        else                   
            return (Box) boxTree.getLastSelectedPathComponent();
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

    //=================================================================================================
    
    private JPanel createStructurePanel()
    {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(1);
        JPanel structurePanel = new JPanel();
        structurePanel.setPreferredSize(new Dimension(200, 408));
        structurePanel.setLayout(gridLayout);
        structurePanel.add(createSidebarPane(), null);
        return structurePanel;
    }

    private JTabbedPane createSidebarPane()
    {
        JTabbedPane sidebarPane = new JTabbedPane();
        sidebarPane.addTab("Box tree", null, createBoxTreePanel(), null);
        return sidebarPane;
    }

    private JPanel createBoxTreePanel()
    {
        GridLayout layout = new GridLayout();
        layout.setRows(1);
        JPanel boxTreePanel = new JPanel();
        boxTreePanel.setLayout(layout);
        
        JScrollPane boxTreeScroll = new JScrollPane();
        boxTreeScroll.setViewportView(getBoxTree());
        boxTreePanel.add(boxTreeScroll, null);
        
        return boxTreePanel;
    }

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
                        browser.getOutputDisplay().drawExtent(node);
                        browser.updateDisplay();
                        //boxTree.scrollPathToVisible(new TreePath(node.getPath()));
                    }
                }
            });
        }
        return boxTree;
    }

    //=================================================================================================
    
    private JPanel createPropertiesPanel()
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.gridx = 0;
        JPanel objectInfoPanel = new JPanel();
        
        GridBagLayout gbl_objectInfoPanel = new GridBagLayout();
        gbl_objectInfoPanel.rowWeights = new double[]{0.0, 0.0};
        gbl_objectInfoPanel.columnWeights = new double[]{1.0};
        objectInfoPanel.setLayout(gbl_objectInfoPanel);
        
        JScrollPane objectInfoScroll = new JScrollPane();
        objectInfoScroll.setViewportView(getInfoTable());
        objectInfoPanel.add(objectInfoScroll, gbc);
        
        return objectInfoPanel;
    }

    private JTable getInfoTable()
    {
        if (infoTable == null)
        {
            infoTable = new JTable();
        }
        return infoTable;
    }

}
