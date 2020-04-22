package cz.vutbr.fit.layout.tools.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cz.vutbr.fit.layout.api.AreaTreeProvider;
import cz.vutbr.fit.layout.impl.DefaultContentRect;
import cz.vutbr.fit.layout.tools.BlockBrowser;
import cz.vutbr.fit.layout.tools.OperatorConfigWindow;
import cz.vutbr.fit.layout.tools.ParamsPanel;

public class SegmentationPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private BlockBrowser browser;

    private JPanel segmChoicePanel;
    private JLabel lblSegmentator;
    private JComboBox<AreaTreeProvider> segmentatorCombo;
    private JButton segmRunButton;
    private JCheckBox segmAutorunCheckbox;
    private JButton btnOperators;
    private ParamsPanel segmParamsPanel;

    protected OperatorConfigWindow operatorWindow;

    public SegmentationPanel(BlockBrowser browser)
    {
        super();
        this.browser = browser;
        GridBagLayout gbl_sourcesTab = new GridBagLayout();
        gbl_sourcesTab.columnWeights = new double[] { 1.0 };
        gbl_sourcesTab.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 1.0 };
        setLayout(gbl_sourcesTab);
        
        GridBagConstraints gbc_segmChoicePanel = new GridBagConstraints();
        gbc_segmChoicePanel.weightx = 1.0;
        gbc_segmChoicePanel.anchor = GridBagConstraints.EAST;
        gbc_segmChoicePanel.fill = GridBagConstraints.BOTH;
        gbc_segmChoicePanel.insets = new Insets(0, 0, 1, 0);
        gbc_segmChoicePanel.gridx = 0;
        gbc_segmChoicePanel.gridy = 2;
        add(getSegmChoicePanel(), gbc_segmChoicePanel);
        
        GridBagConstraints gbc_segmParamsPanel = new GridBagConstraints();
        gbc_segmParamsPanel.insets = new Insets(0, 0, 2, 0);
        gbc_segmParamsPanel.weightx = 1.0;
        gbc_segmParamsPanel.fill = GridBagConstraints.BOTH;
        gbc_segmParamsPanel.gridx = 0;
        gbc_segmParamsPanel.gridy = 3;
        add(getSegmParamsPanel(), gbc_segmParamsPanel);
        
        AreaTreeProvider ap = (AreaTreeProvider) segmentatorCombo.getSelectedItem();
        if (ap != null)
            ((ParamsPanel) segmParamsPanel).setOperation(ap, null);
    }

    //====================================================================================================
    
    public void segmentPage()
    {
        DefaultContentRect.resetId(); //reset the default ID generator to obtain the same IDs for every segmentation
        if (segmentatorCombo.getSelectedIndex() != -1)
        {
            AreaTreeProvider provider = segmentatorCombo.getItemAt(segmentatorCombo.getSelectedIndex());
            browser.segmentPage(provider);
        }
    }
    
    public void reloadServiceParams()
    {
        ((ParamsPanel) getSegmParamsPanel()).reloadParams();
    }
    
    //====================================================================================================
    
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
            Vector<AreaTreeProvider> providers = new Vector<AreaTreeProvider>(browser.getProcessor().getAreaProviders().values());
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
                        operatorWindow = new OperatorConfigWindow(browser.getProcessor());
                    operatorWindow.pack();
                    operatorWindow.setVisible(true);
                }
            });
        }
        return btnOperators;
    }
    
    private JPanel getSegmParamsPanel()
    {
        if (segmParamsPanel == null)
        {
            segmParamsPanel = new ParamsPanel();
        }
        return segmParamsPanel;
    }

}
