/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.impl.BaseBoxTreeBuilder;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.puppeteer.parser.BoxInfo;
import cz.vutbr.fit.layout.puppeteer.parser.InputFile;
import cz.vutbr.fit.layout.puppeteer.parser.PageInfo;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.BackgroundColor;
import cz.vutbr.web.css.CSSProperty.FontFamily;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

/**
 * 
 * @author burgetr
 */
public class BoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(BoxTreeBuilder.class);
    
    public static final String DEFAULT_FONT_FAMILY = "sans-serif";
    public static final float DEFAULT_FONT_SIZE = 12;
    
    /** Input JSON representation */
    private InputFile inputFile;
    
    /** Acceoted generic font families */
    private Set<String> defaultFonts = Set.of("serif", "sans-serif", "monospace");
    
    /** Font families available in the backend browser */ 
    private Set<String> availFonts;
    
    /** The resulting page */
    private PageImpl page;
    

    public BoxTreeBuilder(int width, int height, boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }
    
    public void parse(String urlstring) throws MalformedURLException, IOException
    {
        urlstring = urlstring.trim();
        if (urlstring.startsWith("http:") ||
            urlstring.startsWith("https:") ||
            urlstring.startsWith("ftp:") ||
            urlstring.startsWith("file:"))
        {
            parse(new URL(urlstring));
        }
        else
            throw new MalformedURLException("Unsupported protocol in " + urlstring);
    }
    
    public void parse(URL url) throws IOException
    {
        //get the page data from the backend
        inputFile = invokeRenderer(url);
        availFonts = Set.of(inputFile.getFonts());

        //create the page
        PageInfo pInfo = inputFile.getPage();
        page = new PageImpl(url);
        page.setTitle(pInfo.getTitle());
        page.setWidth(Math.round(pInfo.getWidth()));
        page.setHeight(Math.round(pInfo.getHeight()));
        
        //create the box tree
        List<Box> boxlist = createBoxList(inputFile);
        Box root = buildTree(boxlist, Color.WHITE); //TODO add bgcolor to PageInfo?
        page.setRoot(root);
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }
    
    //==================================================================================
    
    private List<Box> createBoxList(InputFile input)
    {
        List<Box> ret = new ArrayList<>(); //the returned list that includes text boxes
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
                    if (pindex < ret.size())
                    {
                        final Box parent = ret.get(pindex);
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
                    if (pindex < ret.size() && dpindex < ret.size())
                    {
                        final Box parent = ret.get(pindex);
                        final Box domParent = ret.get(dpindex);
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
            ret.add(newbox);
            //The first box is the root box. Ensure it has a background set.
            if (ret.size() == 1)
            {
                if (newbox.getBackgroundColor() == null)
                    newbox.setBackgroundColor(Color.WHITE);
            }
        }
        return ret;
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
        
        ret.setFontFamily(getUsedFont(style, DEFAULT_FONT_FAMILY));
        
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
    
    //==================================================================================
    
    private InputFile invokeRenderer(URL url) throws IOException
    {
        //TODO this is a temporary stub
        FileReader fin = new FileReader(System.getProperty("user.home") + "/tmp/fitlayout/boxes.json");
        Gson gson = new Gson();
        InputFile file = gson.fromJson(fin, InputFile.class);
        fin.close();
        return file;
    }
    
}
