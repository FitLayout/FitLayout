package cz.vutbr.fit.layout.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.process.GUIProcessor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.ListSelectionEvent;

public class OperatorConfigWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private GUIProcessor proc;
    private int uopIndex;
    private JPanel contentPane;
    private JList<AreaTreeOperator> usedList;
    private JTree availList;
    private ParamsPanel paramsPanel;

    public OperatorConfigWindow(GUIProcessor proc)
    {
        this.proc = proc;
        initWindow();
        updateLists();
    }
    
    public void initWindow()
    {
        setTitle("Area Operators");
        setBounds(100, 100, 1200, 600);
        setMinimumSize(new Dimension(450, 300));
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0};
        gbl_contentPane.rowWeights = new double[]{0.0, 1.0, 1.0, 0.0};
        contentPane.setLayout(gbl_contentPane);
        
        JLabel lblUsed = new JLabel("Used");
        GridBagConstraints gbc_lblUsed = new GridBagConstraints();
        gbc_lblUsed.insets = new Insets(0, 0, 5, 5);
        gbc_lblUsed.gridx = 0;
        gbc_lblUsed.gridy = 0;
        contentPane.add(lblUsed, gbc_lblUsed);
        
        JLabel availLabel = new JLabel("Available");
        GridBagConstraints gbc_availLabel = new GridBagConstraints();
        gbc_availLabel.insets = new Insets(0, 0, 5, 0);
        gbc_availLabel.gridx = 2;
        gbc_availLabel.gridy = 0;
        contentPane.add(availLabel, gbc_availLabel);
        
        JScrollPane usedScroll = new JScrollPane();
        GridBagConstraints gbc_usedScroll = new GridBagConstraints();
        gbc_usedScroll.weighty = 1.0;
        gbc_usedScroll.weightx = 1.0;
        gbc_usedScroll.insets = new Insets(0, 0, 5, 5);
        gbc_usedScroll.fill = GridBagConstraints.BOTH;
        gbc_usedScroll.gridx = 0;
        gbc_usedScroll.gridy = 1;
        contentPane.add(usedScroll, gbc_usedScroll);
        
        usedList = new JList<AreaTreeOperator>();
        usedList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) 
            {
                if (usedList.getSelectedValue() != null)
                {
                    AreaTreeOperator uop = usedList.getSelectedValue();
                    uopIndex = usedList.getSelectedIndex();
                    paramsPanel.setOperation(uop, proc.getOperatorParams().elementAt(uopIndex));
                }
            }
        });
        usedScroll.setViewportView(usedList);
        
        JPanel movePanel = new JPanel();
        GridBagConstraints gbc_movePanel = new GridBagConstraints();
        gbc_movePanel.weighty = 1.0;
        gbc_movePanel.insets = new Insets(0, 0, 5, 5);
        gbc_movePanel.fill = GridBagConstraints.BOTH;
        gbc_movePanel.gridx = 1;
        gbc_movePanel.gridy = 1;
        contentPane.add(movePanel, gbc_movePanel);
        GridBagLayout gbl_movePanel = new GridBagLayout();
        gbl_movePanel.columnWeights = new double[]{0.0};
        gbl_movePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
        movePanel.setLayout(gbl_movePanel);
        
        JButton btnUp = new JButton("Up");
        btnUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                int i = usedList.getSelectedIndex();
                if (i > 0)
                {
                    Collections.swap(proc.getSelectedOperators(), i, i - 1);
                    Collections.swap(proc.getOperatorParams(), i, i - 1);
                    uopIndex = i - 1;
                    updateUsedList();
                }
            }
        });
        GridBagConstraints gbc_btnUp = new GridBagConstraints();
        gbc_btnUp.gridx = 0;
        gbc_btnUp.gridy = 0;
        movePanel.add(btnUp, gbc_btnUp);
        
        JButton btnDown = new JButton("Down");
        btnDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                int i = usedList.getSelectedIndex();
                if (i != -1 && i < proc.getSelectedOperators().size() - 1)
                {
                    Collections.swap(proc.getSelectedOperators(), i + 1, i);
                    Collections.swap(proc.getOperatorParams(), i + 1, i);
                    uopIndex = i + 1;
                    updateUsedList();
                }
            }
        });
        GridBagConstraints gbc_btnDown = new GridBagConstraints();
        gbc_btnDown.insets = new Insets(5, 5, 0, 5);
        gbc_btnDown.gridx = 0;
        gbc_btnDown.gridy = 1;
        movePanel.add(btnDown, gbc_btnDown);
        
        JButton btnLeft = new JButton("<<");
        btnLeft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                TreePath selPath = availList.getSelectionPath();
                if (selPath != null)
                {
                    Object sel = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject();
                    if (sel instanceof AreaTreeOperator)
                    {
                        AreaTreeOperator op = (AreaTreeOperator) sel;
                        proc.getSelectedOperators().add(op);
                        proc.getOperatorParams().add(ServiceManager.getServiceParams(op));
                        updateLists();
                    }
                }
            }
        });
        GridBagConstraints gbc_btnLeft = new GridBagConstraints();
        gbc_btnLeft.insets = new Insets(10, 0, 0, 0);
        gbc_btnLeft.gridx = 0;
        gbc_btnLeft.gridy = 2;
        movePanel.add(btnLeft, gbc_btnLeft);
        
        JButton btnRight = new JButton(">>");
        btnRight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                AreaTreeOperator op = usedList.getSelectedValue();
                if (op != null)
                {
                    int index = usedList.getSelectedIndex();
                    proc.getSelectedOperators().remove(index);
                    proc.getOperatorParams().remove(index);
                    updateLists();
                }
            }
        });
        GridBagConstraints gbc_btnRight = new GridBagConstraints();
        gbc_btnRight.insets = new Insets(5, 0, 0, 0);
        gbc_btnRight.gridx = 0;
        gbc_btnRight.gridy = 3;
        movePanel.add(btnRight, gbc_btnRight);
        
        JScrollPane availScroll = new JScrollPane();
        GridBagConstraints gbc_availScroll = new GridBagConstraints();
        gbc_availScroll.weighty = 1.0;
        gbc_availScroll.weightx = 1.0;
        gbc_availScroll.insets = new Insets(0, 0, 5, 0);
        gbc_availScroll.fill = GridBagConstraints.BOTH;
        gbc_availScroll.gridx = 2;
        gbc_availScroll.gridy = 1;
        contentPane.add(availScroll, gbc_availScroll);
        
        availList = new JTree();
        availList.setCellRenderer(new TooltipTreeRenderer());
        javax.swing.ToolTipManager.sharedInstance().registerComponent(availList);
        availScroll.setViewportView(availList);
        
        paramsPanel = new ParamsPanel();
        GridBagConstraints gbc_paramsPanel = new GridBagConstraints();
        gbc_paramsPanel.insets = new Insets(0, 0, 5, 0);
        gbc_paramsPanel.gridwidth = 3;
        gbc_paramsPanel.fill = GridBagConstraints.BOTH;
        gbc_paramsPanel.gridx = 0;
        gbc_paramsPanel.gridy = 2;
        contentPane.add(paramsPanel, gbc_paramsPanel);
        
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });
        GridBagConstraints gbc_btnClose = new GridBagConstraints();
        gbc_btnClose.insets = new Insets(0, 0, 0, 5);
        gbc_btnClose.anchor = GridBagConstraints.EAST;
        gbc_btnClose.gridx = 2;
        gbc_btnClose.gridy = 3;
        contentPane.add(btnClose, gbc_btnClose);
    }

    private void updateLists()
    {
        Vector<AreaTreeOperator> avail = new Vector<AreaTreeOperator>(proc.getOperators().values());
        Collections.sort(avail, new Comparator<AreaTreeOperator>() {
            @Override
            public int compare(AreaTreeOperator o1, AreaTreeOperator o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        availList.setModel(createAvailTree(avail));
        for (int i = 0; i < availList.getRowCount(); i++) {
            availList.expandRow(i);
        }
        updateUsedList();
    }

    private void updateUsedList()
    {
        Vector<AreaTreeOperator> used = proc.getSelectedOperators();
        usedList.setModel(new DefaultComboBoxModel<AreaTreeOperator>(used));
        
        //try to restore selection
        if (uopIndex != -1)
            usedList.setSelectedIndex(uopIndex);
    }
    
    private TreeModel createAvailTree(Collection<AreaTreeOperator> ops)
    {
        //create category map
        Map<String, List<AreaTreeOperator>> opmap = new HashMap<>();
        for (AreaTreeOperator op : ops)
        {
            String cat = op.getCategory();
            List<AreaTreeOperator> oplist = opmap.get(cat);
            if (oplist == null)
            {
                oplist = new ArrayList<>();
                opmap.put(cat, oplist);
            }
            oplist.add(op);
        }
        //create the tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Operators");
        List<String> keys = new ArrayList<>(opmap.keySet());
        Collections.sort(keys);
        for (String cat : keys)
        {
            DefaultMutableTreeNode catnode = new DefaultMutableTreeNode(cat);
            List<AreaTreeOperator> oplist = opmap.get(cat);
            for (AreaTreeOperator op : oplist)
                catnode.add(new DefaultMutableTreeNode(op));
            root.add(catnode);
        }
        //create the model
        return new DefaultTreeModel(root);
    }
    
    protected JList<AreaTreeOperator> getUsedList()
    {
        return usedList;
    }

    protected JTree getAvailList()
    {
        return availList;
    }
    protected ParamsPanel getParamsPanel() {
        return paramsPanel;
    }


    public class TooltipTreeRenderer extends DefaultTreeCellRenderer
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus)
        {
            AreaTreeOperator op = null;
            if (value instanceof DefaultMutableTreeNode)
            {
                Object o = ((DefaultMutableTreeNode) value).getUserObject();
                if (o instanceof AreaTreeOperator)
                    op = (AreaTreeOperator) o;
            }
            else if (value instanceof AreaTreeOperator)
                op = (AreaTreeOperator) value;
            
            final Component rc = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (op != null)
                this.setToolTipText(op.getDescription());
            else
                this.setToolTipText(null);
            return rc;
        }
    }
}
