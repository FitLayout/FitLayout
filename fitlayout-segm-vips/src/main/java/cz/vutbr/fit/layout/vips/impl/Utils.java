/**
 * Utils.java
 *
 * Created on 18. 11. 2020, 11:56:20 by burgetr
 */
package cz.vutbr.fit.layout.vips.impl;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;

/**
 * 
 * @author burgetr
 */
public class Utils
{
    
    public static String colorString(Color color)
    {
        if (color != null)
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        else
            return "";
    }

    public static String fontWeight(Box box)
    {
        return box.getTextStyle().getFontWeight() >= 0.5f ? "bold" : "normal";
    }
    
}
