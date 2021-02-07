/**
 * XMLBoxOutput.java
 *
 * Created on 7. 2. 2021, 12:38:30 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.io.PrintWriter;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Box.Type;

/**
 * Dumps a box tree into a XML file
 * @author burgetr
 */
public class XMLBoxOutput
{
    /** Should we produce the XML header? */
    protected boolean produceHeader;

    
    public XMLBoxOutput(boolean produceHeader)
    {
        this.produceHeader = produceHeader;
    }

    /**
     * Formats the complete tag tree to an output stream
     */
    public void dumpTo(Page tree, PrintWriter out)
    {
        if (produceHeader)
            out.println("<?xml version=\"1.0\"?>");
        //out.println("<areaTree base=\"" + HTMLEntities(tree.getRoot().getPage().getSourceURL().toString()) + "\">");
        out.println("<page>");
        recursiveDump(tree.getRoot(), 1, out);
        out.println("</page>");
    }

    private void recursiveDump(Box root, int level, PrintWriter out)
    {
        String tagName = (root.getTagName() != null) ? root.getTagName() : "#text"; 
        String stag = "<box"
                + " tagName=\"" + tagName + "\""
                
                + " positionX=\"" + root.getBounds().getX1() + "\"" 
                + " positionY=\"" + root.getBounds().getY1() + "\"" 
                + " width=\"" + root.getBounds().getWidth() + "\"" 
                + " height=\"" + root.getBounds().getHeight() + "\""
                + " contentX=\"" + root.getContentBounds().getX1() + "\"" 
                + " contentY=\"" + root.getContentBounds().getY1() + "\"" 
                + " contentWidth=\"" + root.getContentBounds().getWidth() + "\"" 
                + " contentHeight=\"" + root.getContentBounds().getHeight() + "\""
                + " visualX=\"" + root.getVisualBounds().getX1() + "\"" 
                + " visualY=\"" + root.getVisualBounds().getY1() + "\"" 
                + " visualWidth=\"" + root.getVisualBounds().getWidth() + "\"" 
                + " visualHeight=\"" + root.getVisualBounds().getHeight() + "\""
                
                + " color=\"" + colorString(root.getColor()) + "\""
                + " bgcolor=\"" + colorString(root.getBackgroundColor()) + "\""
                + " fontfamily=\"" + root.getFontFamily() + "\""
                + " fontsize=\"" + root.getTextStyle().getFontSize() + "\""
                + " fontweight=\"" + (root.getTextStyle().getFontWeight()) + "\""
                + " fontstyle=\"" + (root.getTextStyle().getFontStyle()) + "\""
                + " underline=\"" + root.getTextStyle().getUnderline() + "\""
                + " linethrough=\"" + root.getTextStyle().getLineThrough() + "\""
                + " replaced=\"" + ((root.getType() == Type.REPLACED_CONTENT)?"true":"false") + "\""
                + ">";

        indent(level, out);
        out.print(stag);
        if (root.getOwnText() == null)
        {
            out.println();
            for (Box child : root.getChildren())
            {
                recursiveDump(child, level + 1, out);
            }
            indent(level, out);
        }
        else
        {
            out.print(HTMLEntities(root.getOwnText()));
        }
        out.println("</box>");
        
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

    private String HTMLEntities(String s)
    {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("&", "&amp;");
    }

}
