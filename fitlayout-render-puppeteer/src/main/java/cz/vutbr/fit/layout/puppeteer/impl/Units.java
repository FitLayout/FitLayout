/**
 * Units.java
 *
 * Created on 24. 3. 2020, 12:12:00 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.web.css.CSSProperty;

/**
 * 
 * @author burgetr
 */
public class Units
{
    
    public static Color toColor(cz.vutbr.web.csskit.Color clr)
    {
        if (clr == null)
            return null;
        else
            return new Color(clr.getRGB());
    }

    public static Box.DisplayType toDisplayType(CSSProperty.Display display)
    {
        final String name = display.name();
        try {
            return Box.DisplayType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
}
