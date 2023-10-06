/**
 * JSONOutputOperator.java
 *
 * Created on 5. 10. 2023, 13:17:26 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.unbescape.json.JsonEscape;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * This operator serializes the area tree to a JSON file.
 * 
 * @author Josef Katrnak
 * @author burgetr
 */
public class JSONOutputOperator extends BaseOperator
{
    /** Path to the output file/ */
    protected String filename;
    
    private int idCount = 0;
    private int labelCount = 0;
    private int countPages = 0;
    private int countPagesSucces = 0;
    private int train = 0;
    boolean[] labels = new boolean[5];

    
    public JSONOutputOperator()
    {
        filename = "out.json";
    }

    public JSONOutputOperator(String filename)
    {
        this.filename = filename;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tools.JSONOutput";
    }

    @Override
    public String getName()
    {
        return "JSON serialization of the area tree";
    }

    @Override
    public String getDescription()
    {
        return "Serializes the area tree to a JSON file";
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
        return ret;
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
            System.err.println("Couldn't create output file " + filename);
        }
    }

    //=====================================================================================================

    /**
     * Dumps input AreTree to JSON.
     *
     * @param atree
     *            input area tree
     * @param writer
     *            output writer
     */
    public void dumpTo(AreaTree atree, PrintWriter writer)
    {
        idCount = 0;
        labelCount = 0;
        countPages++;
        Arrays.fill(labels, Boolean.FALSE);
        recursivelyDumpTo(atree.getRoot(), 1, writer);
    }

    /**
     * Dumps a Page to JSON.
     *
     * @param atree
     *            input area tree
     * @param writer
     *            output writer
     */
    public void dumpTo(Page page, PrintWriter writer)
    {
        idCount = 0;
        labelCount = 0;
        countPages++;
        Arrays.fill(labels, Boolean.FALSE);
        dumpBoxes(List.of(page.getRoot()), 1, writer, true);
    }

    /**
     * Saves Areas to JSON recursively.
     *
     * @param area
     *            input area
     * @param level
     *            area level
     * @param writer
     *            output writer
     */
    private void recursivelyDumpTo(Area area, int level, PrintWriter writer)
    {
        this.idCount++;
        String start =  tab(level - 1) + "{\n" +
                tab(level) + "\"id\": " + idCount + ",\n" +
                tab(level) + "\"level\": " + level + ",\n" +
                tab(level) + "\"x1\": " + area.getX1() + ",\n" +
                tab(level) + "\"y1\": " + area.getY1() + ",\n" +
                tab(level) + "\"x2\": " + area.getX2() + ",\n" +
                tab(level) + "\"y2\": " + area.getY2() + ",\n" +
                tab(level) + "\"height\": " + area.getHeight()+ ",\n" +
                tab(level) + "\"width\": " + area.getWidth() + ",\n" +
                tab(level) + "\"background\": \"" + this.getColor(area.getEffectiveBackgroundColor()) + "\",\n" +
                tab(level) + "\"fontsize\": " + area.getTextStyle().getFontSize() + ",\n" +
                tab(level) + "\"fontweight\": " + area.getTextStyle().getFontWeight() + ",\n" +
                tab(level) + "\"exp_separated\": " + (area.isExplicitlySeparated() ? "true" : "false")  + ",\n" +
                tab(level) + "\"hor_separator\": " + (area.isHorizontalSeparator() ? "true" : "false")  + ",\n" +
                tab(level) + "\"ver_separator\": " + (area.isVerticalSeparator() ? "true" : "false")  + ",\n" +
                tab(level) + "\"fontstyle\": " + area.getTextStyle().getFontStyle();
        String end = "\n" + tab(level - 1) +"}";

        if (area.getChildCount() == 0)
        {
            start += ",\n" + tab(level) + "\"boxes\": [\n";
            end = tab(level) + "]" + end;
            writer.print(start);
            dumpBoxes(area.getBoxes(), level + 1, writer, false);
            writer.print(end);
        }
        else
        {
            start += ",\n" + tab(level) + "\"areas\": [\n";
            end = "]" + end;

            writer.print(start);

            for (int i = 0; i < area.getChildCount(); ++i)
            {
                this.recursivelyDumpTo(area.getChildAt(i), level + 1, writer);
                if (i < area.getChildCount() - 1)
                {
                    writer.print(",\n");
                }
            }

            writer.print(end);
        }
    }

    /**
     * Saves Boxes to JSON.
     *
     * @param area
     *            input area
     * @param level
     *            area level
     * @param writer
     *            output writer
     */
    private void dumpBoxes(List<Box> boxes, int level, PrintWriter writer, boolean recursive)
    {
        Iterator<Box> iBoxes = boxes.iterator();

        while (iBoxes.hasNext())
        {
            Box box = iBoxes.next();
            int label = 0;
            if (this.train == 1)
            {
                label = getLabel(box);
                if (label != 0)
                {
                    this.labels[label - 1] = true;
                }
                if (label != 0)
                {
                    this.labelCount++;
                }
            }
            Rectangular pos = box.getVisualBounds();
            String sBox = tab(level) + "{\n" +
                    tab(level+1) + "\"x1\": " + pos.getX1() + ",\n" +
                    tab(level+1) + "\"y1\": " + pos.getY1() + ",\n" +
                    tab(level+1) + "\"x2\": " + pos.getX2() + ",\n" +
                    tab(level+1) + "\"y2\": " + pos.getY2() + ",\n" +
                    tab(level+1) + "\"height\": " + pos.getHeight()+ ",\n" +
                    tab(level+1) + "\"width\": " + pos.getWidth() + ",\n" +
                    tab(level+1) + "\"color\": \"" + this.getColor(box.getColor()) + "\",\n" +
                    tab(level+1) + "\"fontfamily\": \"" + box.getFontFamily() + "\",\n" +
                    tab(level+1) + "\"fontsize\": " + box.getTextStyle().getFontSize() + ",\n" +
                    tab(level+1) + "\"fontweight\": " + box.getTextStyle().getFontWeight() + ",\n" +
                    tab(level+1) + "\"fontstyle\": " + box.getTextStyle().getFontStyle() + ",\n" +
                    tab(level+1) + "\"underline\": " + box.getTextStyle().getUnderline() + ",\n" +
                    tab(level+1) + "\"linethrough\": " + box.getTextStyle().getLineThrough() + ",\n" +
                    tab(level+1) + "\"type\": " + box.getType().ordinal() + ",\n" +
                    //tab(level+1) + "\"exp_separated\": " + (area.isExplicitlySeparated() ? "true" : "false")  + ",\n" +
                    //tab(level+1) + "\"hor_separator\": " + (area.isHorizontalSeparator() ? "true" : "false")  + ",\n" +
                    //tab(level+1) + "\"ver_separator\": " + (area.isVerticalSeparator() ? "true" : "false")  + ",\n" +
                    tab(level+1) + "\"displayType\": " + box.getDisplayType().ordinal() + ",\n" +
                    tab(level+1) + "\"tag\": \"" + box.getTagName() + "\",\n" +
                    tab(level+1) + "\"visible\": \"" + (box.isVisible() ? "true" : "false")+ "\",\n" +
                    tab(level+1) + "\"separated\": \"" + (box.isVisuallySeparated() ? "true" : "false") + "\",\n";
            if (recursive)
            {
                // for recursive dump only use the own text of the box
                if (box.getOwnText() != null)
                    sBox += tab(level+1) + "\"text\": \"" + JsonEscape.escapeJson(box.getOwnText()) + "\"";
            }
            else
            {
                // for non-recursive dump use the complete text
                sBox += tab(level+1) + "\"text\": \"" + JsonEscape.escapeJson(box.getText()) + "\"";
            }
            if (train == 1)
            {
                sBox += ",\n" + tab(level + 1) + "\"label\": " + label;
            }
            writer.print(sBox);
            
            String eBox = tab(level) + "\n" + tab(level) + "}";
            if (recursive && box.getChildCount() > 0)
            {
                writer.print(tab(level+1) + "\"boxes\": [\n");
                dumpBoxes(box.getChildren(), level + 2, writer, recursive);
                writer.print(tab(level+1) + "]\n");
            }
            
            if (iBoxes.hasNext())
            {
                eBox += ",";
            }
            eBox += "\n";
            writer.print(eBox);
        }
    }

    /**
     * Get klarna label for given box. 0 - no label 1 - Name 2 - Price 3 - Main
     * picture 4 - Add to cart 5 - Cart
     *
     * @param box
     *            input box
     * @return label as int
     */
    private int getLabel(Box box)
    {
        Box tempBox = box;
        while (tempBox != null)
        {
            String label = tempBox.getAttribute("klarna-ai-label");
            if (label != null)
            {
                switch (label)
                {
                    case "Name":
                        return 1;
                    case "Price":
                        return 2;
                    case "Main picture":
                        return 3;
                    case "Add to cart":
                        return 4;
                    case "Cart":
                        return 5;
                    default:
                        return 0;
                }
            }
            tempBox = tempBox.getIntrinsicParent();
        }
        return 0;
    }

    /**
     * Returns color as string.
     *
     * @param color
     *            input color
     * @return color as String
     */
    private String getColor(Color color)
    {
        return color == null ? ""
                : String.format("#%02x%02x%02x", color.getRed(),
                        color.getGreen(), color.getBlue());
    }

    private String tab(int count)
    {
        return "\t".repeat(Math.max(0, count));
    }

    /**
     * Calculates successfully pages (pages with name and price).
     *
     * @return Percentage of success
     */
    public double getSuccessRate()
    {
        if (this.train == 1)
        {
            return (double) countPagesSucces / (double) countPages;
        }
        else
        {
            return 0;
        }
    }

    public int getLabelCount()
    {
        return labelCount;
    }    
    
}
