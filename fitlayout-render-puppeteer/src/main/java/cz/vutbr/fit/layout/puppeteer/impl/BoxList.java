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
import java.util.stream.Collectors;

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
    
    /**
     * Gets a list of all visible boxes.
     * @return
     */
    public List<Box> getVisibleBoxes()
    {
        return boxes.stream()
                .filter(box -> box.isVisible())
                .collect(Collectors.toCollection(ArrayList::new));
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
                //a standard element box
                newbox = createElementBox(boxInfo, style, nextOrder++);
            }
            else
            {
                //text boxes
                newbox = createTextBox(boxInfo, style, nextOrder++);
            }
            //map the offset parent if any, the coordinates are computed from the parent
            if (newbox.getOffsetParent() != null)
            {
                final Rectangular parentBounds = newbox.getOffsetParent().getIntrinsicBounds();
                newbox.getIntrinsicBounds().move(parentBounds.getX1(), parentBounds.getY1());
                newbox.applyIntrinsicBounds();
            }
            //apply clipping when applicable
            if (newbox.getClipBox() != null)
            {
                final Rectangular clipBounds = newbox.getClipBox().getIntrinsicBounds();
                final Rectangular clipped = newbox.getIntrinsicBounds().intersection(clipBounds);
                newbox.setIntrinsicBounds(clipped);
                newbox.applyIntrinsicBounds();
                if (clipped.isEmpty())
                    newbox.setVisible(false);
            }
            //add the box
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
    
    /**
     * Creates an element box.
     * @param src the source box info 
     * @param style the parsed box style
     * @param order  index of the box in the list
     * @return the new box
     */
    private BoxImpl createElementBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl(this);
        setupCommonProperties(ret, src, style, order);
        setupParents(ret, src);
        ret.setType(Box.Type.ELEMENT);
        ret.setTagName(src.getTagName());
        
        CSSProperty.Overflow overflowx = style.getProperty("overflow-x");
        CSSProperty.Overflow overflowy = style.getProperty("overflow-y");
        ret.setClipping((overflowx != null && overflowx != CSSProperty.Overflow.VISIBLE)
                || (overflowy != null && overflowy != CSSProperty.Overflow.VISIBLE));
        
        CSSProperty.BackgroundColor color = style.getProperty("background-color");
        if (color == CSSProperty.BackgroundColor.color)
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

    /**
     * Creates a text box.
     * @param src the source box info (its getText() value must not be null) 
     * @param style the parsed box style
     * @param order  index of the box in the list
     * @return the new box
     */
    private BoxImpl createTextBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl(this);
        setupCommonProperties(ret, src, style, order);
        setupParents(ret, src);
        ret.setType(Box.Type.TEXT_CONTENT);
        
        if (src.getText() != null)
        {
            TextStyle tstyle = new CSSTextStyle(src, style, src.getText().length());
            ((BoxImpl) ret).setIntrinsicTextStyle(tstyle);
            ret.setTextStyle(tstyle);
            ret.setOwnText(src.getText());
        }
        return ret;
    }
    
    /**
     * Sets the common properties of the box based on the style.
     * @param box the destination box
     * @param boxInfo source box info
     * @param style the parsed box style
     * @param order index of the box in the list
     */
    private void setupCommonProperties(BoxImpl box, BoxInfo boxInfo, NodeData style, int order)
    {
        box.setOrder(order);
        box.setId(order);
        box.setIntrinsicBounds(new Rectangular(Math.round(boxInfo.getX()), Math.round(boxInfo.getY()),
                Math.round(boxInfo.getX() + boxInfo.getWidth() - 1), Math.round(boxInfo.getY() + boxInfo.getHeight() - 1)));
        box.applyIntrinsicBounds();
        
        CSSProperty.Position pos = style.getProperty("position");
        if (pos == CSSProperty.Position.ABSOLUTE)
            box.setAbsolute(true);
        else if (pos == CSSProperty.Position.FIXED)
            box.setFixed(true);
        
        box.setFontFamily(getUsedFont(style, BoxTreeBuilder.DEFAULT_FONT_FAMILY));
        
        CSSProperty.Color color = style.getProperty("color");
        if (color == CSSProperty.Color.color)
        {
            TermColor colorVal = style.getValue(TermColor.class, "color", false);
            if (colorVal != null)
                box.setColor(Units.toColor(colorVal.getValue()));
        }
    }
    
    /**
     * Finds and sets the DOM and offset parent for the box. Moreover, the intrinsic parent is set
     * accordinggly: for absolute positoned boxes, the offsetParent is used. For fixed elements,
     * the root box is used. Otherwise, the DOM parent is used.
     * @param box the destination box to which the parents should be assigned
     * @param boxInfo the source box info
     */
    private void setupParents(BoxImpl box, BoxInfo boxInfo)
    {
        if (boxInfo.getParent() != null)
        {
            final int pindex = boxInfo.getParent();
            if (pindex < boxes.size())
            {
                final BoxImpl parent = (BoxImpl) boxes.get(pindex);
                box.setOffsetParent(parent);
            }
            else
            {
                log.error("Backend data error: the offset parent element <{}> is not available for <{}>.", pindex, boxInfo.getId());
            }
        }
        if (boxInfo.getDomParent() != null)
        {
            final int pindex = boxInfo.getDomParent();
            if (pindex < boxes.size())
            {
                final BoxImpl parent = (BoxImpl) boxes.get(pindex);
                box.setDomParent(parent);
            }
            else
            {
                log.error("Backend data error: the DOM parent element <{}> is not available for <{}>.", pindex, boxInfo.getId());
            }
        }
        //compute the intrinsic parent
        if (box.isAbsolute())
        {
            if (box.getOffsetParent() != null)
                box.setIntrinsicParent(box.getOffsetParent());
            else
                log.error("Backend data error: absolutely positioned box <{}> has no offset parent", box.getOrder());
        }
        else if (box.isFixed())
        {
            if (boxes.size() > 0)
                box.setIntrinsicParent(boxes.get(0)); //use the root box
            else
                log.warn("Backend data warning: root box <{}> has a fixed position", box.getOrder());
        }
        else
        {
            if (box.getDomParent() != null)
                box.setIntrinsicParent(box.getDomParent());
            else if (boxes.size() > 0)
                log.error("Backend data error: absolutely positioned box <{}> has no DOM parent", box.getOrder());
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
        CSSProperty.FontFamily ff = style.getProperty("font-family");
        if (ff == CSSProperty.FontFamily.list_values)
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
        {
            if (ff != null)
                return ff.toString();
            else
                return fallback;
        }
    }

    private NodeData parseCss(String css) 
    {
        String ssheet = "* { " + css + "}";
        ssheet = ssheet.replace("text-decoration-line", "text-decoration");
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
