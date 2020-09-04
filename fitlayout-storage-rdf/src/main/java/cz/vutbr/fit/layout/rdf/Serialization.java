/**
 * Serialization.java
 *
 * Created on 4. 9. 2020, 13:55:39 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import cz.vutbr.fit.layout.model.Color;

/**
 * 
 * @author burgetr
 */
public class Serialization
{
    
    public static String colorString(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color decodeHexColor(String colorStr) 
    {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    } 
    
}
