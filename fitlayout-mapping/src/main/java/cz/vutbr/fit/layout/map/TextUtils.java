/**
 * TextUtils.java
 *
 * Created on 3. 6. 2022, 13:42:17 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.util.stream.Collectors;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;

/**
 * 
 * @author burgetr
 */
public class TextUtils
{
    
    /**
     * Creates a complete text of an area by concatenating the subareas and boxes.
     * This is independent on the Area.getText() implementation that may include
     * separating spaces.
     *  
     * @param area
     * @return
     */
    public static String getText(Area area)
    {
        if (area.isLeaf())
        {
            return area.getBoxes()
                .stream()
                .map(Box::getOwnText)
                .collect(Collectors.joining());
        }
        else
        {
            return area.getChildren()
                .stream()
                .map((child) -> getText(child))
                .collect(Collectors.joining());
        }
    }

}
