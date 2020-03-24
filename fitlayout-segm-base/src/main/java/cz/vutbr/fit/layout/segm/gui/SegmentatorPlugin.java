/**
 * SegmentatorPlugin.java
 *
 * Created on 17. 2. 2015, 13:33:24 by burgetr
 */
package cz.vutbr.fit.layout.segm.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;

import cz.vutbr.fit.layout.gui.AreaSelectionListener;
import cz.vutbr.fit.layout.gui.Browser;
import cz.vutbr.fit.layout.gui.BrowserPlugin;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.segm.AreaImpl;
import cz.vutbr.fit.layout.segm.Config;
import cz.vutbr.fit.layout.segm.op.Separator;
import cz.vutbr.fit.layout.segm.op.SeparatorSet;

/**
 * 
 * @author burgetr
 */
public class SegmentatorPlugin implements BrowserPlugin, AreaSelectionListener
{
    private Browser browser;

    private JPanel sepListPanel;
    private JScrollPane sepScroll;
    private JList<Separator> sepList;
    private JToolBar showToolBar;
    private JButton showSepButton;
    private JButton gridButton = null;
    
    @Override
    public boolean init(Browser browser)
    {
        this.browser = browser;
        initGui();
        return true;
    }
    
    private void initGui()
    {
        browser.addStructurePanel("Separators", getSepListPanel());
        browser.addToolBar(getShowToolBar());
        browser.addAreaSelectionListener(this);
    }
    
    //========================================================================================
    
    @Override
    public void areaSelected(Area area)
    {
        //show the separator list
        if (area instanceof AreaImpl)
        {
            SeparatorSet sset = Config.createSeparators((AreaImpl) area);
            DefaultListModel<Separator> ml = new DefaultListModel<Separator>();
            for (Separator sep : sset.getHorizontal())
                ml.addElement(sep);
            for (Separator sep : sset.getVertical())
                ml.addElement(sep);
            for (Separator sep : sset.getBoxsep())
                ml.addElement(sep);
            sepList.setModel(ml);
        }
        else
        {
            //not a grouping area -- no separators
            sepList.setModel(new DefaultListModel<Separator>());
        }
    }

    /**
     * Draws a single separator in the browser.
     * @param sep the separator to be drawn
     */
    private void drawSeparator(Separator sep)
    {
        final Color color = sep.isHorizontal() ? Color.BLUE : Color.RED;
        browser.getOutputDisplay().drawRectangle(sep, color);
    }

    /** 
     * Draws all separators from the currently selected area.
     */
    private void showSeparators()
    {
        ListModel<Separator> ml = sepList.getModel();
        for (int i = 0; i < ml.getSize(); i++)
            drawSeparator(ml.getElementAt(i));
        browser.updateDisplay();
    }
    
    //========================================================================================
    
    private JPanel getSepListPanel()
    {
        if (sepListPanel == null)
        {
            GridLayout gridLayout3 = new GridLayout();
            gridLayout3.setRows(1);
            gridLayout3.setColumns(1);
            sepListPanel = new JPanel();
            sepListPanel.setLayout(gridLayout3);
            sepListPanel.add(getSepScroll(), null);
        }
        return sepListPanel;
    }

    private JScrollPane getSepScroll()
    {
        if (sepScroll == null)
        {
            sepScroll = new JScrollPane();
            sepScroll.setViewportView(getSepList());
        }
        return sepScroll;
    }

    private JList<Separator> getSepList()
    {
        if (sepList == null)
        {
            sepList = new JList<Separator>();
            sepList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
            {
                public void valueChanged(javax.swing.event.ListSelectionEvent e)
                {
                    Separator sep = (Separator) sepList.getSelectedValue();
                    if (sep != null)
                    {
                        drawSeparator(sep);
                        browser.updateDisplay();
                    }
                }
            });
        }
        return sepList;
    }

    private JToolBar getShowToolBar()
    {
        if (showToolBar == null)
        {
            showToolBar = new JToolBar("Segmentation");
            showToolBar.add(getShowSepButton());
            showToolBar.add(getGridButton());
        }
        return showToolBar;
    }
    
    private JButton getShowSepButton()
    {
        if (showSepButton == null)
        {
            showSepButton = new JButton();
            showSepButton.setText("Separators");
            showSepButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    showSeparators();
                }
            });
        }
        return showSepButton;
    }

    /**
     * This method initializes gridButton   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getGridButton()
    {
        if (gridButton == null)
        {
            gridButton = new JButton();
            gridButton.setText("Show grid");
            gridButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    Area node = browser.getSelectedArea();
                    if (node != null)
                    {
                        node.getTopology().drawLayout(browser.getOutputDisplay());
                        browser.updateDisplay();
                    }
                }
            });
        }
        return gridButton;
    }


    
}
