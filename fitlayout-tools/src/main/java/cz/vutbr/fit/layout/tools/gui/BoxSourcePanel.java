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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cz.vutbr.fit.layout.api.BoxTreeProvider;
import cz.vutbr.fit.layout.tools.BlockBrowser;
import cz.vutbr.fit.layout.tools.ParamsPanel;

public class BoxSourcePanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    
    private BlockBrowser browser;
    
    private JPanel rendererChoicePanel;
    private JLabel rendererLabel;
    private JComboBox<BoxTreeProvider> rendererCombo;
    private ParamsPanel rendererParamsPanel;
    private JButton okButton;

    /**
     * Create the panel.
     */
    public BoxSourcePanel(BlockBrowser browser)
    {
        super();
        this.browser = browser;
        GridBagLayout gbl_sourcesTab = new GridBagLayout();
        gbl_sourcesTab.columnWeights = new double[] { 1.0 };
        gbl_sourcesTab.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 1.0 };
        setLayout(gbl_sourcesTab);
        GridBagConstraints gbc_rendererChoicePanel = new GridBagConstraints();
        gbc_rendererChoicePanel.weightx = 1.0;
        gbc_rendererChoicePanel.anchor = GridBagConstraints.EAST;
        gbc_rendererChoicePanel.fill = GridBagConstraints.BOTH;
        gbc_rendererChoicePanel.insets = new Insets(0, 0, 1, 0);
        gbc_rendererChoicePanel.gridx = 0;
        gbc_rendererChoicePanel.gridy = 0;
        add(getRendererChoicePanel(), gbc_rendererChoicePanel);
        GridBagConstraints gbc_rendererParamsPanel = new GridBagConstraints();
        gbc_rendererParamsPanel.weightx = 1.0;
        gbc_rendererParamsPanel.fill = GridBagConstraints.BOTH;
        gbc_rendererParamsPanel.insets = new Insets(0, 0, 2, 0);
        gbc_rendererParamsPanel.gridx = 0;
        gbc_rendererParamsPanel.gridy = 1;
        add(getRendererParamsPanel(), gbc_rendererParamsPanel);

        BoxTreeProvider p = (BoxTreeProvider) rendererCombo.getSelectedItem();
        if (p != null)
            ((ParamsPanel) rendererParamsPanel).setOperation(p, null);
    }
    
    //====================================================================================================
    
    public void setUrl(String url)
    {
        ((ParamsPanel) rendererParamsPanel).setParam("url", url);
    }
    
    public String getUrl()
    {
        return (String) ((ParamsPanel) rendererParamsPanel).getParam("url");
    }
    
    public void reloadServiceParams()
    {
        ((ParamsPanel) getRendererParamsPanel()).reloadParams();
    }
    
    public void displaySelectedURL()
    {
        int i = rendererCombo.getSelectedIndex();
        if (i != -1)
        {
            BoxTreeProvider btp = rendererCombo.getItemAt(i);
            browser.renderPage(btp, ((ParamsPanel) rendererParamsPanel).getParams());
        }
    }
    
    //====================================================================================================
    
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
            Vector<BoxTreeProvider> providers = new Vector<BoxTreeProvider>(browser.getProcessor().getBoxProviders().values());
            DefaultComboBoxModel<BoxTreeProvider> model = new DefaultComboBoxModel<BoxTreeProvider>(providers);
            rendererCombo.setModel(model);
        }
        return rendererCombo;
    }

    private JPanel getRendererParamsPanel()
    {
        if (rendererParamsPanel == null)
        {
            rendererParamsPanel = new ParamsPanel();
        }
        return rendererParamsPanel;
    }
    
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
}
