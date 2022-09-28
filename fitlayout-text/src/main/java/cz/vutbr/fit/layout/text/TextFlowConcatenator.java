/**
 * TextFlowConcatenator.java
 *
 * Created on 28. 9. 2022, 12:25:55 by burgetr
 */
package cz.vutbr.fit.layout.text;

import java.util.List;

import cz.vutbr.fit.layout.api.AreaConcatenator;
import cz.vutbr.fit.layout.api.BoxConcatenator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A concatenator that considers the changes in the text flow when deciding
 * whether to serarate to contents by spaces or not. Basically, in-flow elements
 * should not be separated, block elements and new lines should be separated.
 * 
 * @author burgetr
 */
public class TextFlowConcatenator implements AreaConcatenator
{
    private TextFlowBoxConcatenator boxConcatenator;

    public TextFlowConcatenator()
    {
        boxConcatenator = new TextFlowBoxConcatenator();
    }
    
    @Override
    public BoxConcatenator getBoxConcatenator()
    {
        return boxConcatenator;
    }

    @Override
    public String concat(List<Area> elems)
    {
        final StringBuilder ret = new StringBuilder();
        Area prevArea = null;
        for (Area a : elems)
        {
            final String areaText = a.getText(this);
            if (prevArea != null && shouldSeparate(prevArea, a))
                ret.append(' ');
            ret.append(areaText);
            prevArea = a;
        }
        return ret.toString();
    }

    private static boolean shouldSeparate(ContentRect a1, ContentRect a2)
    {
        final Rectangular b1 = a1.getBounds();
        final Rectangular b2 = a2.getBounds();
        return (b2.getY1() > b1.midY()) || (b2.getY2() < b1.getX1()); //an important change in Y coords
    }
    
    //==========================================================================================
    
    public static class TextFlowBoxConcatenator implements BoxConcatenator
    {

        @Override
        public String concat(List<Box> elems)
        {
            final StringBuilder ret = new StringBuilder();
            Box prevBox = null;
            for (Box  b : elems)
            {
                final String areaText = b.getText(this);
                if (prevBox != null && shouldSeparate(prevBox, b))
                    ret.append(' ');
                ret.append(areaText);
                prevBox = b;
            }
            return ret.toString();
        }
        
    }
    
}
