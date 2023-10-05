/**
 * ArtifactStreamOutput.java
 *
 * Created on 8. 7. 2021, 12:58:34 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * Utility functions for dumping artifacts to an output stream in different formats.
 * 
 * @author burgetr
 */
public class ArtifactStreamOutput
{

    /**
     * Serializes a page to an output stream in XML format.
     * @param page the source page
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputXML(Page page, OutputStream os) throws IOException
    {
        PrintWriter out = new PrintWriter(os);
        XMLBoxOutput xml = new XMLBoxOutput(true);
        xml.dumpTo(page, out);
        out.close();
    }
    
    /**
     * Serializes a page to an output stream in HTML format.
     * @param page the source page
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputHTML(Page page, OutputStream os) throws IOException
    {
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(page, out);
        out.close();
    }

    /**
     * Serializes a page to an output stream as a PNG image using a screen shot if available.
     * If the screenshot is not available, an internal representation of the page is rendered
     * instead.
     * @param page the source page
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputPNG(Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        if (page.getPngImage() != null)
            disp.drawPage(page, true);
        else
            disp.drawPage(page, false);
        disp.saveTo(os);
    }
    
    /**
     * Serializes a page to an output stream as a PNG image by rendering the internal representation
     * of the page.
     * @param page the source page
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputPNGi(Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        disp.drawPage(page, false);
        disp.saveTo(os);
    }

    /**
     * Serializes an area tree to an output stream in XML format.
     * @param atree the source area tree
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputXML(AreaTree atree, OutputStream os) throws IOException
    {
        XMLOutputOperator out = new XMLOutputOperator(null, true);
        PrintWriter writer = new PrintWriter(os);
        out.dumpTo(atree, writer);
        writer.close();
    }
    
    /**
     * Serializes an area tree to an output stream in JSON format.
     * @param atree the source area tree
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputJSON(AreaTree atree, OutputStream os) throws IOException
    {
        JSONOutputOperator out = new JSONOutputOperator(null);
        PrintWriter writer = new PrintWriter(os);
        out.dumpTo(atree, writer);
        writer.close();
    }
    
    /**
     * Serializes an area tree to an output stream in HTML format.
     * @param atree the source area tree
     * @param page the page artifact the area tree was created from
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputHTML(AreaTree atree, Page page, OutputStream os) throws IOException
    {
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(atree, page, out);
        out.close();
    }

    /**
     * Serializes an area tree to an output stream as a PNG image using a screen shot if available.
     * If the screenshot is not available, an internal representation of the page is rendered
     * instead.
     * @param atree the source area tree
     * @param page the page artifact the area tree was created from
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputPNG(AreaTree atree, Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        if (page.getPngImage() != null)
            disp.drawPage(page, true);
        else
            disp.drawPage(page, false);
        showAreas(disp, atree.getRoot(), null);
        disp.saveTo(os);
    }

    /**
     * Serializes an area tree to an output stream as a PNG image by rendering the internal
     * representation of the page.
     * @param atree the source area tree
     * @param page the page artifact the area tree was created from
     * @param os the target output stream
     * @throws IOException
     */
    public static void outputPNGi(AreaTree atree, Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        disp.drawPage(page, false);
        showAreas(disp, atree.getRoot(), null);
        disp.saveTo(os);
    }

    private static void showAreas(OutputDisplay disp, Area root, String nameSubstring)
    {
        if (nameSubstring == null || root.toString().contains(nameSubstring))
            disp.drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAreas(disp, root.getChildAt(i), nameSubstring);
    }

}
