/**
 * HTMLOutputOperator.java
 *
 * Created on 12. 1. 2016, 11:42:43 by burgetr
 */
package cz.vutbr.fit.layout.tools.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Box.Type;

/**
 * This operator serializes the area tree to an HTML file.
 * 
 * @author burgetr
 */
public class HTMLOutputOperator extends BaseOperator
{
    /** Default length unit */
    protected static final String UNIT = "px";
    
    /** Should we produce the HTML header and footer? */
    protected boolean produceHeader;
    
    /** Should we produce the box tree only or should we rely on the area tree? */
    protected boolean boxTreeOnly;
    
    /** Path to the output file/ */
    protected String filename;
    
    
    public HTMLOutputOperator()
    {
        produceHeader = true;
        boxTreeOnly = false;
        filename = "out.html";
    }

    public HTMLOutputOperator(String filename, boolean produceHeader, boolean boxTreeOnly)
    {
        this.filename = filename;
        this.produceHeader = produceHeader;
        this.boxTreeOnly = boxTreeOnly;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tools.HTMLOutput";
    }

    @Override
    public String getName()
    {
        return "HTML serialization of the area tree";
    }

    @Override
    public String getDescription()
    {
        return "Serializes the area tree to an HTML file";
    }

    @Override
    public String getCategory()
    {
        return "output";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(3);
        ret.add(new ParameterString("filename"));
        ret.add(new ParameterBoolean("produceHeader"));
        ret.add(new ParameterBoolean("boxTreeOnly"));
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

    public boolean getBoxTreeOnly() 
    {
		return boxTreeOnly;
	}

	public void setBoxTreeOnly(boolean boxTreeOnly) 
	{
		this.boxTreeOnly = boxTreeOnly;
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
            Page page = (Page) getServiceManager().getArtifactRepository().getArtifact(atree.getParentIri());
            PrintWriter out = new PrintWriter(filename);
            if (boxTreeOnly)
                dumpTo(page, out);
            else
                dumpTo(atree, page, out);
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't create output HTML file " + filename);
        }
    }

    //=====================================================================================================
    
    /**
     * Formats the complete area tree to an output stream.
     * @param tree the area tree to be printed
     * @param out a writer to be used for output
     */
    public void dumpTo(AreaTree tree, Page sourcePage, PrintWriter out)
    {
        if (produceHeader)
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            if (sourcePage != null)
                out.println("<title>" + sourcePage.getTitle() + "</title>");
            out.println("<meta charset=\"utf-8\">");
            out.println("<meta name=\"generator\" content=\"FITLayout - area tree dump\">");
            out.println("</head>");
            out.println("<body>");
        }
        recursiveDumpArea(tree.getRoot(), 1, out);
        if (produceHeader)
        {
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    /**
     * Formats the complete box tree to an output stream.
     * @param tree the area tree to be printed
     * @param out a writer to be used for output
     */
    public void dumpTo(Page page, PrintWriter out)
    {
        if (produceHeader)
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>" + page.getTitle() + "</title>");
            out.println("<meta charset=\"utf-8\">");
            out.println("<meta name=\"generator\" content=\"FITLayout - box tree dump\">");
            out.println("</head>");
            out.println("<body>");
        }
        recursiveDumpBoxes(page.getRoot(), 1, out);
        if (produceHeader)
        {
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    //=====================================================================
    
    private void recursiveDumpArea(Area a, int level, java.io.PrintWriter p)
    {
        String tagName = "div";
        
        String stag = "<" + tagName
                        + " id=\"a" + a.getId() + "\""
                        + " style=\"" + getAreaStyle(a) + "\""
                        + ">";

        String etag = "</" + tagName + ">";
        
        if (a.getChildCount() > 0)
        {
            indent(level, p);
            p.println(stag);
            
            for (int i = 0; i < a.getChildCount(); i++)
                recursiveDumpArea(a.getChildAt(i), level+1, p);
            
            indent(level, p);
            p.println(etag);
        }
        else
        {
            indent(level, p);
            p.println(stag);
            dumpAreaBoxes(a, p, level+1);
            indent(level, p);
            p.println(etag);
        }
        
    }
    
    private void dumpAreaBoxes(Area a, java.io.PrintWriter p, int level)
    {
        Vector<Box> boxes = a.getBoxes();
        for (Box box : boxes)
        {
            //Dump only the text boxes. The style of the element boxes should be
            //already taken into account in the areas.
            if (box.getType() == Type.TEXT_CONTENT)
            {
                indent(level, p);
                String stag = "<span"
                                + " id=\"b" + box.getId() + "\""
                                + " style=\"" + getBoxStyle(a, box) + "\"" 
                                + ">";
                p.print(stag);
                p.print(HTMLEntities(box.getText()));
                p.println("</span>");
            }
        }
    }
    
    private void recursiveDumpBoxes(Box box, int level, java.io.PrintWriter p)
    {
        if (box.getType() == Type.TEXT_CONTENT)
        {
            indent(level, p);
            String stag = "<span"
                            + " id=\"b" + box.getId() + "\""
                            + " style=\"" + getBoxStyle(box.getParent(), box) + "\"" 
                            + ">";
            p.print(stag);
            p.print(HTMLEntities(box.getText()));
            p.println("</span>");
        }
        else
        {
            Style style = getBoxStyle(box.getParent(), box);
            style.put("width", getContentWidth(box), "px");
            style.put("height", getContentHeight(box), "px");
            String stag = "<div"
                            + " id=\"b" + box.getId() + "\""
                            + " style=\"" + style + "\"" 
                            + ">";
            indent(level, p);
            p.println(stag);
            
            for (int i = 0; i < box.getChildCount(); i++)
            	recursiveDumpBoxes(box.getChildAt(i), level + 1, p);
            
            indent(level, p);
            p.println("</div>");
        }
    }
    
    protected String getAreaStyle(Area a)
    {
        Area parent = a.getParent();
        int px = 0;
        int py = 0;
        if (parent != null)
        {
            px = parent.getX1();
            py = parent.getY1();
            
            Border bleft = parent.getBorderStyle(Border.Side.LEFT);
            if (bleft != null)
                px += bleft.getWidth();
            Border btop = parent.getBorderStyle(Border.Side.TOP);
            if (btop != null)
                py += btop.getWidth();
        }

        int bw = 0;
        int bh = 0;
        Style style = new Style();
        style.put("position", "absolute");
        String bgcol = colorString(a.getBackgroundColor());
        if (!bgcol.isEmpty())
            style.put("background", bgcol);
        for (Border.Side side : Border.Side.values())
        {
            Border bstyle = a.getBorderStyle(side);
            String brd = getBorderStyle(bstyle);
            if (!brd.isEmpty())
            {
                style.put("border-" + side.toString(), brd);
                if (side == Border.Side.LEFT || side == Border.Side.RIGHT)
                    bw += bstyle.getWidth();
                else if (side == Border.Side.TOP || side == Border.Side.BOTTOM)
                    bh += bstyle.getWidth();
            }
        }
        style.put("left", a.getX1() - px, UNIT);
        style.put("top", a.getY1() - py, UNIT);
        style.put("width", a.getWidth() - bw, UNIT);
        style.put("height", a.getHeight() - bh, UNIT);
        
        return style.toString();
    }
    
    protected Style getBoxStyle(ContentRect parent, Box box)
    {
        int px = 0;
        int py = 0;
        if (parent != null)
        {
            px = parent.getX1() + parent.getBorderStyle(Border.Side.LEFT).getWidth();
            py = parent.getY1() + parent.getBorderStyle(Border.Side.TOP).getWidth();
        }
        return getBoxStyle(box, px, py);
    }

	protected Style getBoxStyle(Box box, int px, int py) 
	{
		Rectangular pos = box.getVisualBounds();
        Style style = new Style();
        style.put("position", "absolute");
        style.put("top", (pos.getY1() - py), UNIT);
        style.put("left", (pos.getX1() - px), UNIT);
        style.put("color", (colorString(box.getColor())));
        String bgcol = colorString(box.getBackgroundColor());
        if (!bgcol.isEmpty())
            style.put("background", bgcol);
        style.put("font-family", box.getFontFamily());
        style.put("font-size", box.getFontSize(), UNIT);
        style.put("font-weight", ((box.getFontWeight() < 0.5f)?"normal":"bold"));
        style.put("font-style", ((box.getFontStyle() < 0.5f)?"normal":"italic"));
        String deco = "";
        if (box.getUnderline() >= 0.5f)
            deco += "underline";
        if (box.getLineThrough() >= 0.5f)
            deco += " line-through";
        if (deco.isEmpty())
            deco = "none";
        style.put("text-decoration", deco);
        for (Border.Side side : Border.Side.values())
        {
            String brd = getBorderStyle(box.getBorderStyle(side));
            if (!brd.isEmpty())
                style.put("border-" + side.toString(), brd);
        }
		return style;
	}
    
    private String getBorderStyle(Border border)
    {
        if (border != null && border.getStyle() != Border.Style.NONE && border.getWidth() > 0)
        {
            StringBuilder ret = new StringBuilder();
            ret.append(border.getWidth()).append(UNIT);
            ret.append(' ').append(border.getStyle().toString().toLowerCase());
            ret.append(' ').append(colorString(border.getColor()));
            return ret.toString();
        }
        else
            return "";
    }
    
    private int getContentWidth(Box box)
    {
        return box.getWidth() - box.getLeftBorder() - box.getRightBorder();
    }
    
    private int getContentHeight(Box box)
    {
        return box.getHeight() - box.getTopBorder() - box.getBottomBorder();
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
    
    private String HTMLEntities(String s)
    {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("&", "&amp;");
    }
    
    /**
     * Element style representation.
     * 
     * @author burgetr
     */
    protected class Style extends HashMap<String, String>
    {
        private static final long serialVersionUID = 1L;
        
        public void put(String key, int value, String unit)
        {
            put(key, value + unit);
        }
        
        public void put(String key, float value, String unit)
        {
            put(key, value + unit);
        }
        
        @Override
        public String toString()
        {
            StringBuilder ret = new StringBuilder();
            for (Map.Entry<String, String> entry : entrySet())
            {
                ret.append(entry.getKey()).append(':').append(entry.getValue()).append(';');
            }
            return ret.toString();
        }
        
    }

}
