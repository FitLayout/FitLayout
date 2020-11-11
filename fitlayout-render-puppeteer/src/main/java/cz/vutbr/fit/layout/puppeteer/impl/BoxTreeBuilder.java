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
import cz.vutbr.web.css.CSSProperty.FontFamily;
import cz.vutbr.web.css.CSSProperty.FontSize;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.css.CSSProperty.FontWeight;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermList;

/**
 * 
 * @author burgetr
 */
public class BoxTreeBuilder extends BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(BoxTreeBuilder.class);
    
    private static final String DEFAULT_FONT_FAMILY = "sans-serif";
    private static final float DEFAULT_FONT_SIZE = 12;
    
    /** Input JSON representation */
    private InputFile inputFile;
    
    /** Acceoted generic font families */
    private Set<String> defaultFonts = Set.of("serif", "sans-serif", "monospace");
    
    /** Font families available in the backend browser */ 
    private Set<String> availFonts;
    
    /** The resulting page */
    private PageImpl page;
    

    public BoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
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
        List<Box> elems = new ArrayList<>(input.getBoxes().length); //list of elements for assigning the parents
        //create the element and text boxes
        int nextOrder = 0;
        for (BoxInfo boxInfo : input.getBoxes())
        {
            final NodeData style = parseCss(boxInfo.getCss());
            final BoxImpl elem = createElementBox(boxInfo, style, nextOrder++);
            elems.add(elem);
            //map the parent if any
            if (boxInfo.getParent() != null)
            {
                final int pindex = boxInfo.getParent();
                if (pindex < elems.size())
                    elem.setIntrinsicParent(elems.get(pindex));
            }
            ret.add(elem);
            //create a text box if there is a contained text
            if (boxInfo.getText() != null)
            {
                final BoxImpl tbox = createTextBox(boxInfo, style, nextOrder++);
                tbox.setIntrinsicParent(elem);
                ret.add(tbox);
            }
        }
        return ret;
    }
    
    private BoxImpl createElementBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl();
        ret.setType(Box.Type.ELEMENT);
        ret.setOrder(order);
        ret.setId(order);
        ret.setFontFamily(getUsedFont(style, DEFAULT_FONT_FAMILY));
        ret.setIntrinsicBounds(new Rectangular(Math.round(src.getX()), Math.round(src.getY()),
                Math.round(src.getX() + src.getWidth() - 1), Math.round(src.getY() + src.getHeight() - 1)));
        
        
        return ret;
    }

    private BoxImpl createTextBox(BoxInfo src, NodeData style, int order)
    {
        BoxImpl ret = new BoxImpl();
        ret.setType(Box.Type.TEXT_CONTENT);
        ret.setOrder(order);
        ret.setId(order);
        ret.setFontFamily(getUsedFont(style, DEFAULT_FONT_FAMILY));
        if (src.getText() != null)
        {
            TextStyle tstyle = createTextStyle(style, src.getText().length());
            ((BoxImpl) ret).setIntrinsicTextStyle(tstyle);
            ret.setTextStyle(tstyle);
            ret.setOwnText(src.getText());
        }
        return ret;
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
                final String name = value.toString();
                if (availFonts.contains(name) || defaultFonts.contains(name))
                    return name;
            }
            return fallback;
        }
        else
            return ff.toString();
    }

    private TextStyle createTextStyle(NodeData style, int textLen)
    {
        TextStyle ret = new TextStyle();
        
        ret.setContentLength(textLen);
        
        FontSize fsize = style.getProperty("font-size");
        if (fsize == FontSize.length)
        {
            TermLength fsizeVal = style.getValue(TermLength.class, "font-size", false);
            ret.setFontSizeSum(fsizeVal.getValue());
        }
        else
            ret.setFontSizeSum(DEFAULT_FONT_SIZE);
        
        FontWeight fweight = style.getProperty("font-weight");
        switch (fweight)
        {
            case BOLD:
            case BOLDER:
            case numeric_600:
            case numeric_700:
            case numeric_800:
            case numeric_900:
                ret.setFontWeightSum(1);
                break;
            default:
                break;
        }
        
        FontStyle fstyle = style.getProperty("font-style");
        if (fstyle == FontStyle.ITALIC || fstyle == FontStyle.OBLIQUE)
            ret.setFontStyleSum(1);
        
        //TODO text decoration
        
        
        return ret;
    }
    
    private NodeData parseCss(String css) 
    {
        final String ssheet = "* { " + css + "}";
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
