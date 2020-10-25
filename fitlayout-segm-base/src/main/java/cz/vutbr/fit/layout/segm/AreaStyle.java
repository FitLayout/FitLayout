/**
 * AreaStyle.java
 *
 * Created on 29.6.2012, 12:43:15 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;

/**
 * This class represents the complete style of an area for further comparison.
 * 
 * @author burgetr
 */
public class AreaStyle
{
    private float averageFontSize;
    private float averageFontWeight;
    private float averageFontStyle;
    private float averageColorLuminosity;
    private Color backgroundColor;
    
    public AreaStyle(float averageFontSize, float averageFontWeight,
            float averageFontStyle, float averageColorLuminosity,
            Color backgroundColor)
    {
        this.averageFontSize = averageFontSize;
        this.averageFontWeight = averageFontWeight;
        this.averageFontStyle = averageFontStyle;
        this.averageColorLuminosity = averageColorLuminosity;
        this.backgroundColor = backgroundColor;
    }
    
    public AreaStyle(Area source)
    {
        this.averageFontSize = source.getTextStyle().getFontSize();
        this.averageFontWeight = source.getTextStyle().getFontWeight();
        this.averageFontStyle = source.getTextStyle().getFontStyle();
        this.averageColorLuminosity = computeColorLuminosity(source);
        this.backgroundColor = source.getBackgroundColor();
    }

    public double getAverageFontSize()
    {
        return averageFontSize;
    }

    public void setAverageFontSize(float averageFontSize)
    {
        this.averageFontSize = averageFontSize;
    }

    public double getAverageFontWeight()
    {
        return averageFontWeight;
    }

    public void setAverageFontWeight(float averageFontWeight)
    {
        this.averageFontWeight = averageFontWeight;
    }

    public double getAverageFontStyle()
    {
        return averageFontStyle;
    }

    public void setAverageFontStyle(float averageFontStyle)
    {
        this.averageFontStyle = averageFontStyle;
    }

    public double getAverageColorLuminosity()
    {
        return averageColorLuminosity;
    }

    public void setAverageColorLuminosity(float averageColorLuminosity)
    {
        this.averageColorLuminosity = averageColorLuminosity;
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }
    
    
    /**
     * Compares two styles and decides if it is the same style. The thresholds of the style are taken from the {@link Config}.
     * @param other the other area to be compared
     * @return <code>true</code> if the areas are considered to have the same style
     */
    public boolean isSameStyle(AreaStyle other)
    {
        double fsdif = Math.abs(getAverageFontSize() - other.getAverageFontSize());
        double wdif = Math.abs(getAverageFontWeight() - other.getAverageFontWeight());
        double sdif = Math.abs(getAverageFontStyle() - other.getAverageFontStyle());
        double ldif = Math.abs(getAverageColorLuminosity() - other.getAverageColorLuminosity());
        Color bg1 = getBackgroundColor();
        Color bg2 = other.getBackgroundColor();
        
        return fsdif <= Config.FONT_SIZE_THRESHOLD 
                && wdif <= Config.FONT_WEIGHT_THRESHOLD
                && sdif <= Config.FONT_STYLE_THRESHOLD
                && ldif <= Config.TEXT_LUMINOSITY_THRESHOLD
                && ((bg1 == null && bg2 == null) || (bg1 != null && bg2 != null && bg1.equals(bg2)));
    }
    
    //====================================================================================
    
    /**
     * Compares two visual areas and checks whether they have the same visual style.
     * @param a1 the first area to compare
     * @param a2 the second area to compare
     * @return {@code true} when a2 has the same visual style as a1
     */
    public static boolean hasSameStyle(Area a1, Area a2)
    {
        final AreaStyle s1 = new AreaStyle(a1);
        final AreaStyle s2 = new AreaStyle(a2);
        return s1.isSameStyle(s2);
    }
    
    /**
     * Checks if two areas have the same background color
     * @param a1 the first area to compare
     * @param a1 the second area to compare
     * @return {@code true} if the areas are both transparent or they have the same
     * background color declared
     */
    public static boolean hasEqualBackground(Area a1, Area a2)
    {
        return (a1.getBackgroundColor() == null && a2.getBackgroundColor() == null) || 
               (a1.getBackgroundColor() != null && a2.getBackgroundColor() != null 
                   && a1.getBackgroundColor().equals(a2.getBackgroundColor()));
    }

    
    //====================================================================================
    
    public float computeColorLuminosity(Area area)
    {
        if (area.getBoxes().isEmpty())
            return 0;
        else
        {
            float sum = 0;
            int len = 0;
            for (Box box : area.getBoxes())
            {
                int l = box.getText().length(); 
                sum += colorLuminosity(box.getColor()) * l;
                len += l;
            }
            return sum / len;
        }
    }

    private float colorLuminosity(Color c)
    {
        float lr, lg, lb;
        if (c == null)
        {
            lr = lg = lb = 255;
        }
        else
        {
            lr = (float) Math.pow(c.getRed() / 255.0f, 2.2f);
            lg = (float) Math.pow(c.getGreen() / 255.0f, 2.2f);
            lb = (float) Math.pow(c.getBlue() / 255.0f, 2.2f);
        }
        return lr * 0.2126f +  lg * 0.7152f + lb * 0.0722f;
    }

    
}
