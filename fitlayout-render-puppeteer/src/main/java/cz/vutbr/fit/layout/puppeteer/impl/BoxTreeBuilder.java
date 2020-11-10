/**
 * BoxTreeBuilder.java
 *
 * Created on 6. 11. 2020, 8:32:27 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Page;
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
import cz.vutbr.web.css.TermList;

/**
 * 
 * @author burgetr
 */
public class BoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(BoxTreeBuilder.class);
    
    private InputFile inputFile;
    private Set<String> defaultFonts = Set.of("serif", "sans-serif", "monospace");
    private Set<String> availFonts;
    

    public BoxTreeBuilder(InputFile inputFile)
    {
        this.inputFile = inputFile;
        availFonts = Set.of(inputFile.getFonts());
    }
    
    public Page buildPage()
    {
        return null; //TODO
    }
    
    //==================================================================================
    
    private Box createBox(BoxInfo src)
    {
        NodeData style = parseCss(src.getCss());
        
        BoxImpl ret = new BoxImpl();
        ret.setFontFamily(getUsedFont(style, "sans-serif"));
        //ret.setTextStyle(textStyle);
        
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
        CSSProperty.FontFamily ff = style.getProperty("font-family");
        if (ff == CSSProperty.FontFamily.list_values)
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

    private TextStyle getTextStyle(NodeData style)
    {
        TextStyle ret = new TextStyle();
        
        
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
    
}
