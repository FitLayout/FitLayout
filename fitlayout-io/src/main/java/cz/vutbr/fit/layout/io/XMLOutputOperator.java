/**
 * XMLOutputOperator.java
 *
 * Created on 2. 2. 2015, 13:04:11 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.Box.Type;

/**
 * This operator serializes the area tree to an XML file.
 *  
 * @author burgetr
 */
public class XMLOutputOperator extends BaseOperator
{
    /** Should we produce the XML header? */
    protected boolean produceHeader;
    
    /** Path to the output file/ */
    protected String filename;
    
    private int idcnt = 0;

    
    public XMLOutputOperator()
    {
        produceHeader = false;
        filename = "out.xml";
    }

    public XMLOutputOperator(String filename, boolean produceHeader)
    {
        this.filename = filename;
        this.produceHeader = produceHeader;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tools.XMLOutput";
    }

    @Override
    public String getName()
    {
        return "XML serialization of the area tree";
    }

    @Override
    public String getDescription()
    {
        return "Serializes the area tree to an XML file";
    }

    @Override
    public String getCategory()
    {
        return "output";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterString("filename"));
        ret.add(new ParameterBoolean("produceHeader"));
        return ret;
    }
    
    public boolean getProduceHeader()
    {
        return produceHeader;
    }

    public void setProduceHeader(boolean produceHeader)
    {
        this.produceHeader = produceHeader;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    //=====================================================================================================
    
    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        try
        {
            PrintWriter out = new PrintWriter(filename);
            dumpTo(atree, out);
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't create output XML file " + filename);
        }
    }

    //=====================================================================================================
    
    /**
     * Formats the complete tag tree to an output stream
     */
    public void dumpTo(AreaTree tree, PrintWriter out)
    {
        if (produceHeader)
            out.println("<?xml version=\"1.0\"?>");
        //out.println("<areaTree base=\"" + HTMLEntities(tree.getRoot().getPage().getSourceURL().toString()) + "\">");
        out.println("<areaTree>");
        recursiveDump(tree.getRoot(), 1, out);
        out.println("</areaTree>");
    }
    
    //=====================================================================
    
    private void recursiveDump(Area a, int level, java.io.PrintWriter p)
    {
        String stag = "<area"
                        + " id=\"x" + (idcnt++) + "\""
                        + " x1=\"" + a.getX1() + "\"" 
                        + " y1=\"" + a.getY1() + "\"" 
                        + " x2=\"" + a.getX2() + "\"" 
                        + " y2=\"" + a.getY2() + "\"" 
                        + " background=\"" + colorString(a.getBackgroundColor()) + "\"" 
                        + " fontsize=\"" + a.getTextStyle().getFontSize() + "\"" 
                        + " fontweight=\"" + a.getTextStyle().getFontWeight() + "\"" 
                        + " fontstyle=\"" + a.getTextStyle().getFontStyle() + "\""
                        + " tags=\"" + tagString(a.getTags().keySet()) + "\""
                        + ">";

        String etag = "</area>";
        
        if (a.getChildCount() > 0)
        {
            indent(level, p);
            p.println(stag);
            
            for (int i = 0; i < a.getChildCount(); i++)
                recursiveDump(a.getChildAt(i), level+1, p);
            
            indent(level, p);
            p.println(etag);
        }
        else
        {
            indent(level, p);
            p.println(stag);
            dumpBoxes(a, p, level+1);
            indent(level, p);
            p.println(etag);
        }
        
    }
    
    private void dumpBoxes(Area a, java.io.PrintWriter p, int level)
    {
        List<Box> boxes = a.getBoxes();
        for (Box box : boxes)
        {
            Rectangular pos = box.getVisualBounds();
            indent(level, p);
            String stag = "<box"
                            + " x1=\"" + pos.getX1() + "\"" 
                            + " y1=\"" + pos.getY1() + "\"" 
                            + " x2=\"" + pos.getX2() + "\"" 
                            + " y2=\"" + pos.getY2() + "\""
                            + " color=\"" + colorString(box.getColor()) + "\""
                            + " fontfamily=\"" + box.getFontFamily() + "\""
                            + " fontsize=\"" + box.getTextStyle().getFontSize() + "\""
                            + " fontweight=\"" + (box.getTextStyle().getFontWeight()) + "\""
                            + " fontstyle=\"" + (box.getTextStyle().getFontStyle()) + "\""
                            + " underline=\"" + box.getTextStyle().getUnderline() + "\""
                            + " linethrough=\"" + box.getTextStyle().getLineThrough() + "\""
                            + " replaced=\"" + ((box.getType() == Type.REPLACED_CONTENT)?"true":"false") + "\""
                            + ">";
            p.print(stag);
            p.print(HTMLEntities(box.getText()));
            p.println("</box>");
        }
    }
    
    private void indent(int level, java.io.PrintWriter p)
    {
        String ind = "";
        for (int i = 0; i < level*4; i++) ind = ind + ' ';
        p.print(ind);
    }
    
    private String colorString(Color color)
    {
        if (color == null)
            return "";
        else
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Converts the CSS specification rgb(r,g,b) to #rrggbb
     * @param spec the CSS color specification
     * @return a #rrggbb string
     */
    public String colorString(String spec)
    {
        if (spec.startsWith("rgb("))
        {
            String s = spec.substring(4, spec.length() - 1);
            String[] lst = s.split(",");
            try {
                int r = Integer.parseInt(lst[0].trim());
                int g = Integer.parseInt(lst[1].trim());
                int b = Integer.parseInt(lst[2].trim());
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else
            return spec;
    }
    
    private String tagString(Set<Tag> tags)
    {
        String ret = "";
        for (Tag tag : tags)
            ret += tag + " ";
        return ret.trim();
    }
    
    private String HTMLEntities(String s)
    {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("&", "&amp;");
    }
    
}
