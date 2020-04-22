package cz.vutbr.fit.layout.tools.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
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

import cz.vutbr.fit.layout.api.LogicalTreeProvider;
import cz.vutbr.fit.layout.tools.BlockBrowser;
import cz.vutbr.fit.layout.tools.ParamsPanel;

public class LogicalPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private BlockBrowser browser;

    private JPanel logicalChoicePanel;
    private ParamsPanel logicalParamsPanel;
    private JLabel lblLogicalBuilder;
    private JComboBox<LogicalTreeProvider> logicalCombo;
    private JButton logicalRunButton;
    private JCheckBox logicalAutorunCheckbox;

    public LogicalPanel(BlockBrowser browser)
    {
        super();
        this.browser = browser;
        
        GridBagConstraints gbc_logicalChoicePanel = new GridBagConstraints();
        gbc_logicalChoicePanel.insets = new Insets(0, 0, 1, 0);
        gbc_logicalChoicePanel.fill = GridBagConstraints.BOTH;
        gbc_logicalChoicePanel.gridx = 0;
        gbc_logicalChoicePanel.gridy = 4;
        add(getLogicalChoicePanel(), gbc_logicalChoicePanel);
        
        GridBagConstraints gbc_logicalParamsPanel = new GridBagConstraints();
        gbc_logicalParamsPanel.fill = GridBagConstraints.BOTH;
        gbc_logicalParamsPanel.gridx = 0;
        gbc_logicalParamsPanel.gridy = 5;
        add(getLogicalParamsPanel(), gbc_logicalParamsPanel);
        
        LogicalTreeProvider lp = (LogicalTreeProvider) logicalCombo.getSelectedItem();
        if (lp != null)
            ((ParamsPanel) logicalParamsPanel).setOperation(lp, null);
    }

    //====================================================================================================
    
    private void buildLogicalTree()
    {
        if (logicalCombo.getSelectedIndex() != -1)
        {
            LogicalTreeProvider provider = logicalCombo.getItemAt(logicalCombo.getSelectedIndex());
            browser.buildLogicalTree(provider);
        }
    }

    //====================================================================================================

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
            Vector<LogicalTreeProvider> providers = new Vector<LogicalTreeProvider>(browser.getProcessor().getLogicalProviders().values());
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

    
}
