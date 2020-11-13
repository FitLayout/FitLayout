/**
 * BoxList.java
 *
 * Created on 13. 11. 2020, 9:38:30 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.puppeteer.parser.BoxInfo;
import cz.vutbr.fit.layout.puppeteer.parser.InputFile;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.CSSProperty.BackgroundColor;
import cz.vutbr.web.css.CSSProperty.FontFamily;

/**
 * A list of FitLayout boxes created from the backend output.
 * 
 * @author burgetr
 */
public class BoxList
{
    private static Logger log = LoggerFactory.getLogger(BoxList.class);
    
    /** Accepted generic font families */
    private Set<String> defaultFonts = Set.of("serif", "sans-serif", "monospace");
    
    /** Font families available in the backend browser */ 
    private Set<String> availFonts;

    /** Managed list of boxes */
    private List<Box> boxes;
    
    
    /**
     * Creates a box list from an input file obtained from the backend.
     * 
     * @param inputFile
     */
    public BoxList(InputFile inputFile)
    {
        availFonts = Set.of(inputFile.getFonts());
        createBoxList(inputFile);
    }

    /**
     * Gets the complete list of boxes.
     * @return
     */
    public List<Box> getBoxes()
    {
        return boxes;
    }
    
    //=======================================================================================
    
    private List<Box> createBoxList(InputFile input)
    {
        boxes = new ArrayList<>();
        //create the element and text boxes
        int nextOrder = 0;
        for (BoxInfo boxInfo : input.getBoxes())
        {
            final NodeData style = parseCss(boxInfo.getCss());
            final BoxImpl newbox;
            if (boxInfo.getText() == null)
            {
                //standard element box
                newbox = createElementBox(boxInfo, style, nextOrder++);
                //map the offset parent if any, the coordinates are computed from the parent
                if (boxInfo.getParent() != null)
                {
                    final int pindex = boxInfo.getParent();
                    if (pindex < boxes.size())
                    {
                        final Box parent = boxes.get(pindex);
                        newbox.setIntrinsicParent(parent);
                        final Rectangular parentBounds = parent.getIntrinsicBounds();
                        newbox.getIntrinsicBounds().move(parentBounds.getX1(), parentBounds.getY1());
                        newbox.applyIntrinsicBounds();
                    }
                    else
                    {
                        log.error("Backend error: the parent element is not yet available.");
                    }
                }
            }
            else
            {
                //text boxes
                newbox = createTextBox(boxInfo, style, nextOrder++);
                //use the DOM parent element. The intrinsic bounds are however relative to the offset parent.
                if (boxInfo.getParent() != null && boxInfo.getDomParent() != null)
                {
                    final int pindex = boxInfo.getParent();
                    final int dpindex = boxInfo.getDomParent();
                    if (pindex < boxes.size() && dpindex < boxes.size())
                    {
                        final Box parent = boxes.get(pindex);
                        final Box domParent = boxes.get(dpindex);
                        newbox.setIntrinsicParent(domParent);
                        final Rectangular parentBounds = parent.getIntrinsicBounds();
                        newbox.getIntrinsicBounds().move(parentBounds.getX1(), parentBounds.getY1());
                        newbox.applyIntrinsicBounds();
                    }
                    else
                    {
                        log.error("Backend error: the parent element is not yet available.");
                    }
                }
                else
                {
                    log.error("Backend error: a text node is missing a parent reference");
                }
            }
            boxes.add(newbox);
            //The first box is the root box. Ensure it has a background set.
            if (boxes.size() == 1)
            {
                if (newbox.getBackgroundColor() == null)
                    newbox.setBackgroundColor(Color.WHITE);
            }
        }
        return boxes;
    }
    
    private BoxImpl createElementBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl();
        setupCommonProperties(ret, src, style, order);
        ret.setType(Box.Type.ELEMENT);
        ret.setTagName(src.getTagName());
        
        BackgroundColor color = style.getProperty("background-color");
        if (color == BackgroundColor.color)
        {
            TermColor colorVal = style.getValue(TermColor.class, "background-color", false);
            if (colorVal != null)
            {
                Color clr = Units.toColor(colorVal.getValue());
                if (clr.getAlpha() > 0)
                    ret.setBackgroundColor(clr); //represent transparent background as null background
            }
        }
        
        for (Border.Side side : Border.Side.values())
        {
            Border brd = new CSSBorder(style, side.toString());
            if (brd.getStyle() != Border.Style.NONE)
                ret.setBorderStyle(side, brd);
        }
        
        return ret;
    }

    private BoxImpl createTextBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl();
        setupCommonProperties(ret, src, style, order);
        ret.setType(Box.Type.TEXT_CONTENT);
        ret.setTagName("text");
        
        if (src.getText() != null)
        {
            TextStyle tstyle = new CSSTextStyle(src, style, src.getText().length());
            ((BoxImpl) ret).setIntrinsicTextStyle(tstyle);
            ret.setTextStyle(tstyle);
            ret.setOwnText(src.getText());
        }
        return ret;
    }
    
    private void setupCommonProperties(BoxImpl ret, BoxInfo src, NodeData style, int order)
    {
        ret.setOrder(order);
        ret.setId(order);
        ret.setIntrinsicBounds(new Rectangular(Math.round(src.getX()), Math.round(src.getY()),
                Math.round(src.getX() + src.getWidth() - 1), Math.round(src.getY() + src.getHeight() - 1)));
        ret.applyIntrinsicBounds();
        
        ret.setFontFamily(getUsedFont(style, BoxTreeBuilder.DEFAULT_FONT_FAMILY));
        
        CSSProperty.Color color = style.getProperty("color");
        if (color == CSSProperty.Color.color)
        {
            TermColor colorVal = style.getValue(TermColor.class, "color", false);
            if (colorVal != null)
                ret.setColor(Units.toColor(colorVal.getValue()));
        }
    }
    
    /**
     * Parses the font-family declaration and finds the effective font used.
     * @param style
     * @param fallback
     * @return
     */
    private String getUsedFont(NodeData style, String fallback)
    {
        FontFamily ff = style.getProperty("font-family");
        if (ff == FontFamily.list_values)
        {
            TermList values = (TermList) style.getValue("font-family", false);
            for (Term<?> value : values)
            {
                if (value instanceof TermString)
                {
                    final String name = ((TermString) value).getValue();
                    if (availFonts.contains(name) || defaultFonts.contains(name))
                        return name;
                }
            }
            return fallback;
        }
        else
            return ff.toString();
    }

    private NodeData parseCss(String css) 
    {
        String ssheet = "* { " + css + "}";
        ssheet = ssheet.replace("text-decoration-line", "text-decoration");
        if (ssheet.contains("underline"))
            System.out.println(ssheet);
        NodeData style = CSSFactory.createNodeData();
        try {
            StyleSheet sheet = CSSFactory.parseString(ssheet, new URL("http://base.url"));
            RuleSet rule = (RuleSet) sheet.get(0);
            for (Declaration d : rule)
            {
                style.push(d);
            }
        } catch (CSSException e) {
            log.error("Couldn't parse inline css: {}", e.getMessage());
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return style;
    }
    
    
}
