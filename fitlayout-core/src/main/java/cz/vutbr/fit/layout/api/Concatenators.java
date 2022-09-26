/**
 * Concatenators.java
 *
 * Created on 26. 9. 2022, 9:46:40 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.List;
import java.util.stream.Collectors;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;

/**
 * Pre-defined common concatenators for joining sequences of boxes and areas.
 * 
 * @author burgetr
 */
public class Concatenators
{
    private static BoxConcatenator defaultBoxConcatenator = new SeparatedBoxConcatenator(" ");
    private static AreaConcatenator defaultAreaConcatenator = new PlainAreaConcatenator(defaultBoxConcatenator);
    
    /**
     * The default box concatenator that concatenates the text content of boxes separated
     * by spaces.
     * @return The box concatenator instance.
     */
    public static BoxConcatenator getDefaultBoxConcatenator()
    {
        return defaultBoxConcatenator;
    }

    /**
     * The default area concatenator that concatenates the area contents with no separators
     * and boxes with the default (space-separated) concatenator.
     * @return The area concatenator instance.
     */
    public static AreaConcatenator getDefaultAreaConcatenator()
    {
        return defaultAreaConcatenator;
    }

    /**
     * A box concatenator that simply concatenates the box text without any
     * separators.
     * 
     * @author burgetr
     */
    public static class PlainBoxConcatenator implements BoxConcatenator
    {
        @Override
        public String concat(List<Box> elems)
        {
            return elems
                    .stream()
                    .map(b -> b.getText(this))
                    .collect(Collectors.joining());
        }
    }

    /**
     * A box concatenator that concatenates the box text using a given
     * separator.
     * 
     * @author burgetr
     */
    public static class SeparatedBoxConcatenator implements BoxConcatenator
    {
        private String separator;

        public SeparatedBoxConcatenator(String separator)
        {
            this.separator = separator;
        }

        @Override
        public String concat(List<Box> elems)
        {
            return elems
                    .stream()
                    .map(b -> b.getText(this))
                    .collect(Collectors.joining(separator));
        }
    }

    /**
     * An area concatenator that simply concatenates the area text contents without any
     * separators.
     * 
     * @author burgetr
     */
    public static class PlainAreaConcatenator implements AreaConcatenator
    {
        private BoxConcatenator boxConcatenator;
        
        public PlainAreaConcatenator(BoxConcatenator boxConcatenator)
        {
            this.boxConcatenator = boxConcatenator;
        }

        @Override
        public String concat(List<Area> elems)
        {
            return elems
                    .stream()
                    .map(a -> a.getText(this))
                    .collect(Collectors.joining());
        }

        @Override
        public BoxConcatenator getBoxConcatenator()
        {
            return boxConcatenator;
        }
    }

    /**
     * An area concatenator that concatenates the area text using a given
     * separator.
     * 
     * @author burgetr
     */
    public static class SeparatedAreaConcatenator implements AreaConcatenator
    {
        private String separator;
        private BoxConcatenator boxConcatenator;

        public SeparatedAreaConcatenator(String separator, BoxConcatenator boxConcatenator)
        {
            this.separator = separator;
            this.boxConcatenator = boxConcatenator;
        }

        @Override
        public String concat(List<Area> elems)
        {
            return elems
                    .stream()
                    .map(b -> b.getText(this))
                    .collect(Collectors.joining(separator));
        }
        
        @Override
        public BoxConcatenator getBoxConcatenator()
        {
            return boxConcatenator;
        }
    }

}
