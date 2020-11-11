/**
 * Units.java
 *
 * Created on 24. 3. 2020, 12:12:00 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import cz.vutbr.fit.layout.model.Color;

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

}
