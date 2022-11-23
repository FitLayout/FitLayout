/**
 * CSSBorder.java
 *
 * Created on 12. 11. 2020, 20:24:39 by burgetr
 */
package cz.vutbr.fit.layout.json.impl;

import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermLength;

/**
 * 
 * @author burgetr
 */
public class CSSBorder extends Border
{

    public CSSBorder(NodeData style, String side)
    {
        TermColor tclr = style.getSpecifiedValue(TermColor.class, "border-"+side+"-color");
        TermLength width = style.getValue(TermLength.class, "border-"+side+"-width");
        CSSProperty.BorderStyle bst = style.getProperty("border-"+side+"-style");
        if (bst != CSSProperty.BorderStyle.NONE && bst != CSSProperty.BorderStyle.HIDDEN)
        {
            switch (bst)
            {
                case DASHED:
                    setStyle(Border.Style.DASHED);
                    break;
                case DOTTED:
                    setStyle(Border.Style.DOTTED);
                    break;
                case DOUBLE:
                    setStyle(Border.Style.DOUBLE);
                    break;
                default:
                    setStyle(Border.Style.SOLID);
                    break;
            }
            setWidth(Math.round(width.getValue()));
            setColor(Units.toColor(tclr.getValue()));
        }
        
    }
    
}
