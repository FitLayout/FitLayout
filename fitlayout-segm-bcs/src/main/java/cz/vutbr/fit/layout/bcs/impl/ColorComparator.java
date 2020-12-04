package cz.vutbr.fit.layout.bcs.impl;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorComparator
{
    private static final double LAB_MAX_DIFF = 258.68384120267046;
    private static final double LCH_MAX_DIFF = 149.93691702034678;
    private static final double RGB_MAX_DIFF = 1.7320508075688772;

    private JSpinner r1 = null;
    private JSpinner g1 = null;
    private JSpinner b1 = null;
    private Color color1 = null;
    private JPanel panel1 = null;

    private JSpinner r2 = null;
    private JSpinner g2 = null;
    private JSpinner b2 = null;
    private Color color2 = null;
    private JPanel panel2 = null;

    private JFrame frame;
    private JTextField rgb;
    private JTextField lab;
    private JTextField lch;

    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ColorComparator window = new ColorComparator();
                    window.frame.setVisible(true);
                    window.recalculate1();
                    window.recalculate2();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getMaxValues()
    {
        /* DOC: since it would take forever to iterate through
         * the entire color space, the trick is to divide by increments
         * of twenty
         */
        double maxDiffLch = 0, maxDiffLab = 0, maxDiffRgb = 0, diff;
        Color c1lch = null, c2lch = null, c1lab = null, c2lab = null, c1rgb = null, c2rgb = null;
        Color c1, c2;

        for (int r = 0; r < 5; r += 1)
        {
            for (int g = 0; g < 5; g += 1)
            {
                for (int b = 0; b < 5; b += 1)
                {
                    for (int r2 = 250; r2 < 256; r2 += 1)
                    {
                        for (int g2 = 250; g2 < 256; g2 += 1)
                        {
                            for (int b2 = 250; b2 < 256; b2 += 1)
                            {
                                c1 = new Color(r, g, b);
                                c2 = new Color(r2, g2, b2);
                                diff = PageArea.colorDiff(c1, c2);
                                if (diff > maxDiffLab) { maxDiffLab = diff; c1lab = c1; c2lab = c2;}
                                diff = PageArea.colorDiffLch(c1, c2);
                                if (diff > maxDiffLch) {maxDiffLch = diff; c1lch = c1; c2lch = c2;}
                                diff = PageArea.colorDiffRgb(c1, c2);
                                if (diff > maxDiffRgb) {maxDiffRgb = diff; c1rgb = c1; c2rgb = c2;}
                            }
                        }
                    }
                }
            }
            System.out.println("Counter R: "+r);
        }
        System.out.println("Lab: "+maxDiffLab+" - "+c1lab.getRed()+","+c1lab.getGreen()+","+c1lab.getBlue()+" - "+c2lab.getRed()+","+c2lab.getGreen()+","+c2lab.getBlue());
        System.out.println("LCH: "+maxDiffLch+" - "+c1lch.getRed()+","+c1lch.getGreen()+","+c1lch.getBlue()+" - "+c2lch.getRed()+","+c2lch.getGreen()+","+c2lch.getBlue());
        System.out.println("RGB: "+maxDiffRgb+" - "+c1rgb.getRed()+","+c1rgb.getGreen()+","+c1rgb.getBlue()+" - "+c2rgb.getRed()+","+c2rgb.getGreen()+","+c2rgb.getBlue());
    }

    /**
     * Create the application.
     */
    public ColorComparator()
    {
        initialize();
    }

    public void recalculate1()
    {
        double drgb, dlab, dlch;
        color1 = new Color((Integer)r1.getValue(), (Integer)g1.getValue(), (Integer)b1.getValue());
        panel1.setBackground(color1);

        if (color2 == null) return;

        drgb = PageArea.colorDiffRgb(color1, color2)/RGB_MAX_DIFF;
        dlab = PageArea.colorDiff(color1, color2)/LAB_MAX_DIFF;
        dlch = PageArea.colorDiffLch(color1, color2)/LCH_MAX_DIFF;
        this.rgb.setText(String.format("%.3f", drgb));
        this.lab.setText(String.format("%.3f", dlab));
        this.lch.setText(String.format("%.3f", dlch));
    }

    public void recalculate2()
    {
        double drgb, dlab, dlch;
        color2 = new Color((Integer)r2.getValue(), (Integer)g2.getValue(), (Integer)b2.getValue());
        panel2.setBackground(color2);

        if (color1 == null) return;

        drgb = PageArea.colorDiffRgb(color1, color2)/RGB_MAX_DIFF;
        dlab = PageArea.colorDiff(color1, color2)/LAB_MAX_DIFF;
        dlch = PageArea.colorDiffLch(color1, color2)/LCH_MAX_DIFF;
        this.rgb.setText(String.format("%.3f", drgb));
        this.lab.setText(String.format("%.3f", dlab));
        this.lch.setText(String.format("%.3f", dlch));
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 264);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {110, 110, 110, 110};
        gridBagLayout.rowHeights = new int[] {0, 20, 20, 20, 20, 20, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        frame.getContentPane().setLayout(gridBagLayout);

        JLabel lblBarva = new JLabel("Barva 1");
        GridBagConstraints gbc_lblBarva = new GridBagConstraints();
        gbc_lblBarva.insets = new Insets(0, 0, 5, 5);
        gbc_lblBarva.gridx = 1;
        gbc_lblBarva.gridy = 0;
        frame.getContentPane().add(lblBarva, gbc_lblBarva);

        JLabel lblBarva_1 = new JLabel("Barva 2");
        GridBagConstraints gbc_lblBarva_1 = new GridBagConstraints();
        gbc_lblBarva_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblBarva_1.gridx = 2;
        gbc_lblBarva_1.gridy = 0;
        frame.getContentPane().add(lblBarva_1, gbc_lblBarva_1);

        panel1 = new JPanel();
//        panel1.setBackground(Color.WHITE);
        panel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(
                        frame,
                        "Choose Background Color",
                        frame.getBackground());

               if(newColor != null){
                   r1.setValue(newColor.getRed());
                   g1.setValue(newColor.getGreen());
                   b1.setValue(newColor.getBlue());
                   recalculate1();
               }
            }
        });
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridheight = 3;
        gbc_panel_1.insets = new Insets(0, 0, 5, 5);
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 1;
        frame.getContentPane().add(panel1, gbc_panel_1);

        panel2 = new JPanel();
//        panel2.setBackground(Color.WHITE);
        panel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(
                        frame,
                        "Choose Background Color",
                        frame.getBackground());

               if(newColor != null){
                   r2.setValue(newColor.getRed());
                   g2.setValue(newColor.getGreen());
                   b2.setValue(newColor.getBlue());
                   recalculate2();
               }
            }
        });
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridheight = 3;
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 3;
        gbc_panel.gridy = 1;
        frame.getContentPane().add(panel2, gbc_panel);


        r1 = new JSpinner();
        r1.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent arg0) {
                int val = ((Integer)r1.getValue()).intValue();
                val -= arg0.getWheelRotation();
                if (val < 0 || val  > 255) return;
                r1.setValue(new Integer(val));
            }
        });
        r1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate1();
            }
        });
        r1.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_r1 = new GridBagConstraints();
        gbc_r1.insets = new Insets(0, 0, 5, 5);
        gbc_r1.gridx = 1;
        gbc_r1.gridy = 1;
        frame.getContentPane().add(r1, gbc_r1);

        r2 = new JSpinner();
        r2.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent arg0) {
                int val = ((Integer)r2.getValue()).intValue();
                val -= arg0.getWheelRotation();
                if (val < 0 || val  > 255) return;
                r2.setValue(new Integer(val));
            }
        });
        r2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate2();
            }
        });
        r2.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_r2 = new GridBagConstraints();
        gbc_r2.insets = new Insets(0, 0, 5, 5);
        gbc_r2.gridx = 2;
        gbc_r2.gridy = 1;
        frame.getContentPane().add(r2, gbc_r2);


        g1 = new JSpinner();
        g1.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = ((Integer)g1.getValue()).intValue();
                val -= e.getWheelRotation();
                if (val < 0 || val  > 255) return;
                g1.setValue(new Integer(val));
            }
        });
        g1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate1();
            }
        });
        g1.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_g1 = new GridBagConstraints();
        gbc_g1.insets = new Insets(0, 0, 5, 5);
        gbc_g1.gridx = 1;
        gbc_g1.gridy = 2;
        frame.getContentPane().add(g1, gbc_g1);

        g2 = new JSpinner();
        g2.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = ((Integer)g2.getValue()).intValue();
                val -= e.getWheelRotation();
                if (val < 0 || val  > 255) return;
                g2.setValue(new Integer(val));
            }
        });
        g2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate2();
            }
        });
        g2.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_g2 = new GridBagConstraints();
        gbc_g2.insets = new Insets(0, 0, 5, 5);
        gbc_g2.gridx = 2;
        gbc_g2.gridy = 2;
        frame.getContentPane().add(g2, gbc_g2);


        b1 = new JSpinner();
        b1.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = ((Integer)b1.getValue()).intValue();
                val -= e.getWheelRotation();
                if (val < 0 || val  > 255) return;
                b1.setValue(new Integer(val));
            }
        });
        b1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate1();
            }
        });
        b1.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_b1 = new GridBagConstraints();
        gbc_b1.insets = new Insets(0, 0, 5, 5);
        gbc_b1.gridx = 1;
        gbc_b1.gridy = 3;
        frame.getContentPane().add(b1, gbc_b1);

        b2 = new JSpinner();
        b2.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int val = ((Integer)b2.getValue()).intValue();
                val -= e.getWheelRotation();
                if (val < 0 || val  > 255) return;
                b2.setValue(new Integer(val));
            }
        });
        b2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                recalculate2();
            }
        });
        b2.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        GridBagConstraints gbc_b2 = new GridBagConstraints();
        gbc_b2.insets = new Insets(0, 0, 5, 5);
        gbc_b2.gridx = 2;
        gbc_b2.gridy = 3;
        frame.getContentPane().add(b2, gbc_b2);


        JLabel lblRgbDiff = new JLabel("RGB diff");
        GridBagConstraints gbc_lblRgbDiff = new GridBagConstraints();
        gbc_lblRgbDiff.insets = new Insets(0, 0, 5, 5);
        gbc_lblRgbDiff.gridx = 0;
        gbc_lblRgbDiff.gridy = 5;
        frame.getContentPane().add(lblRgbDiff, gbc_lblRgbDiff);

        JLabel lblLabDiff = new JLabel("Lab diff");
        GridBagConstraints gbc_lblLabDiff = new GridBagConstraints();
        gbc_lblLabDiff.insets = new Insets(0, 0, 5, 5);
        gbc_lblLabDiff.gridx = 1;
        gbc_lblLabDiff.gridy = 5;
        frame.getContentPane().add(lblLabDiff, gbc_lblLabDiff);

        JLabel lblLchDiff = new JLabel("LCH diff");
        GridBagConstraints gbc_lblLchDiff = new GridBagConstraints();
        gbc_lblLchDiff.insets = new Insets(0, 0, 5, 5);
        gbc_lblLchDiff.gridx = 2;
        gbc_lblLchDiff.gridy = 5;
        frame.getContentPane().add(lblLchDiff, gbc_lblLchDiff);

        rgb = new JTextField();
        rgb.setEditable(false);
        GridBagConstraints gbc_rgb = new GridBagConstraints();
        gbc_rgb.insets = new Insets(0, 0, 5, 5);
        gbc_rgb.fill = GridBagConstraints.HORIZONTAL;
        gbc_rgb.gridx = 0;
        gbc_rgb.gridy = 6;
        frame.getContentPane().add(rgb, gbc_rgb);
        rgb.setColumns(10);

        lab = new JTextField();
        lab.setEditable(false);
        GridBagConstraints gbc_lab = new GridBagConstraints();
        gbc_lab.insets = new Insets(0, 0, 5, 5);
        gbc_lab.fill = GridBagConstraints.HORIZONTAL;
        gbc_lab.gridx = 1;
        gbc_lab.gridy = 6;
        frame.getContentPane().add(lab, gbc_lab);
        lab.setColumns(10);

        lch = new JTextField();
        lch.setEditable(false);
        GridBagConstraints gbc_lch = new GridBagConstraints();
        gbc_lch.insets = new Insets(0, 0, 5, 5);
        gbc_lch.fill = GridBagConstraints.HORIZONTAL;
        gbc_lch.gridx = 2;
        gbc_lch.gridy = 6;
        frame.getContentPane().add(lch, gbc_lch);
        lch.setColumns(10);
    }

}
