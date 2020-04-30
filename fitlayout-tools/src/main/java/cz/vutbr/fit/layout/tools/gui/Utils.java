/**
 * Utils.java
 *
 * Created on 30. 4. 2020, 11:29:38 by burgetr
 */
package cz.vutbr.fit.layout.tools.gui;

import java.util.Map;

import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Tag;

/**
 * Various GUI utility functions.
 * 
 * @author burgetr
 */
public class Utils
{

    public static String colorString(Color color)
    {
        if (color == null)
            return "- transparent -";
        else
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String borderString(ContentRect a)
    {
        String bs = "";
        if (a.hasTopBorder()) bs += "^";
        if (a.hasLeftBorder()) bs += "<";
        if (a.hasRightBorder()) bs += ">";
        if (a.hasBottomBorder()) bs += "_";
        return bs;
    }
    
    public static String tagString(Map<Tag, Float> tags)
    {
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<Tag, Float> entry : tags.entrySet())
            ret.append(entry.getKey()).append(" ");
        return ret.toString();
    }
    
}
