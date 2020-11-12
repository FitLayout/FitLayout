/**
 * CSSTextStyle.java
 *
 * Created on 12. 11. 2020, 11:05:07 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.puppeteer.parser.BoxInfo;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.CSSProperty.FontSize;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.css.CSSProperty.FontWeight;

/**
 * Text style obtained from a CSS style definition.
 * 
 * @author burgetr
 */
public class CSSTextStyle extends TextStyle
{

    public CSSTextStyle(BoxInfo src, NodeData style, int textLen)
    {
        super();
        setContentLength(textLen);
        
        FontSize fsize = style.getProperty("font-size");
        if (fsize == FontSize.length)
        {
            TermLength fsizeVal = style.getValue(TermLength.class, "font-size", false);
            setFontSizeSum(fsizeVal.getValue() * textLen);
        }
        else
            setFontSizeSum(BoxTreeBuilder.DEFAULT_FONT_SIZE * textLen);
        
        FontWeight fweight = style.getProperty("font-weight");
        switch (fweight)
        {
            case BOLD:
            case BOLDER:
            case numeric_600:
            case numeric_700:
            case numeric_800:
            case numeric_900:
                setFontWeightSum(1 * textLen);
                break;
            default:
                break;
        }
        
        FontStyle fstyle = style.getProperty("font-style");
        if (fstyle == FontStyle.ITALIC || fstyle == FontStyle.OBLIQUE)
            setFontStyleSum(1  * textLen);

        if (src.getDecoration() != null)
        {
            if (src.getDecoration().contains("U"))
                setUnderlineSum(1 * textLen);
            if (src.getDecoration().contains("T"))
                setLineThroughSum(1 * textLen);
        }
    }
    
}
