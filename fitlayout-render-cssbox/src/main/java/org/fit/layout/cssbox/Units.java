/**
 * Units.java
 *
 * Created on 24. 3. 2020, 12:12:00 by burgetr
 */
package org.fit.layout.cssbox;

import org.fit.layout.model.Color;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class Units
{
    
    public static Color toColor(java.awt.Color clr)
    {
        if (clr == null)
            return null;
        else
            return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), clr.getAlpha());
    }
    
    public static Color toColor(cz.vutbr.web.csskit.Color clr)
    {
        if (clr == null)
            return null;
        else
            return new Color(clr.getRGB());
    }

    public static Rectangular toRectangular(java.awt.Rectangle src)
    {
        if (src == null)
            return null;
        else
            return new Rectangular(src.x, src.y, src.x + src.width - 1, src.y + src.height - 1);
    }
    
}
