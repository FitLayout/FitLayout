/**
 * LogicalTab.java
 *
 * Created on 22. 4. 2020, 18:51:35 by burgetr
 */
package cz.vutbr.fit.layout.tools.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.tools.AreaTreeModel;
import cz.vutbr.fit.layout.tools.BlockBrowser;
import cz.vutbr.fit.layout.tools.BrowserPanel;
import cz.vutbr.fit.layout.tools.LogicalTreeModel;

/**
 * 
 * @author burgetr
 */
public class LogicalTab extends BrowserTabBase
{
    private JTree logicalTree;
    

    public LogicalTab(BlockBrowser browser)
    {
        super(browser);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getTitle()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JPanel getTabPanel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JPanel getStructurePanel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JPanel getPropertiesPanel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reloadServiceParams()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void refreshView()
    {
        //reloads the logical area tree from the processor
        if (browser.getProcessor().getLogicalAreaTree() != null)
        {
            TreePath path = logicalTree.getSelectionPath();
            logicalTree.setModel(new LogicalTreeModel(browser.getProcessor().getLogicalAreaTree().getRoot()));
            if (path != null)
                logicalTree.setSelectionPath(path);
        }
        
    }
    //=================================================================================================

    private JPanel createLogicalTreePanel()
    {
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.fill = GridBagConstraints.BOTH;
        gridBagConstraints8.gridy = 0;
        gridBagConstraints8.weightx = 1.0;
        gridBagConstraints8.weighty = 1.0;
        gridBagConstraints8.gridx = 0;
        JPanel logicalTreePanel = new JPanel();
        logicalTreePanel.setLayout(new GridBagLayout());
        
        JScrollPane logicalTreeScroll = new JScrollPane();
        logicalTreeScroll.setViewportView(getLogicalJTree());
        logicalTreePanel.add(logicalTreeScroll, gridBagConstraints8);
        return logicalTreePanel;
    }

    private JTree getLogicalJTree()
    {
        if (logicalTree == null)
        {
            logicalTree = new JTree();
            logicalTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
                    {
                        public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                        {
                            /*if (areasync)
                            {
                                LogicalArea node = (LogicalArea) logicalTree.getLastSelectedPathComponent();
                                if (node != null)
                                {
                                    showLogicalArea((LogicalArea) node);
                                    logsync = false;
                                    showAreaInTree(((LogicalArea) node).getFirstArea());
                                    logsync = true;
                                }
                            }*/
                        }
                    });
            logicalTree.setModel(new LogicalTreeModel(null));
        }
        return logicalTree;
    }

    
    private void showLogicalArea(LogicalArea lnode)
    {
        /*boolean first = true;
        for (Area area : lnode.getAreas())
        {
            if (first)
                showArea(area);
            else
                ((BrowserPanel) contentCanvas).getOutputDisplay().drawExtent(area);
            first = false;
        }
        contentCanvas.repaint();*/
    }

    private void showAreaInLogicalTree(Area node)
    {
        if (browser.getProcessor().getLogicalAreaTree() != null && browser.getProcessor().getLogicalAreaTree().getRoot() != null)
        {
            LogicalArea lnode = browser.getProcessor().getLogicalAreaTree().getRoot().findArea(node);
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
    

    
}
