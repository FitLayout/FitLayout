/**
 * TextStyle.java
 *
 * Created on 25. 10. 2020, 8:24:21 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * This class represent the statistics about text style of a content rectangle.
 * 
 * @author burgetr
 */
public class TextStyle
{
    private float fontSizeSum;
    private float fontWeightSum;
    private float fontStyleSum;
    private float underlineSum;
    private float lineThroughSum;
    private int contentLength;
    

    public TextStyle()
    {
        fontSizeSum = 0;
        fontWeightSum = 0;
        fontStyleSum = 0;
        underlineSum = 0;
        lineThroughSum = 0;
        contentLength = 0;
    }
    
    public TextStyle(TextStyle src)
    {
        fontSizeSum = src.fontSizeSum;
        fontWeightSum = src.fontWeightSum;
        fontStyleSum = src.fontStyleSum;
        underlineSum = src.underlineSum;
        lineThroughSum = src.lineThroughSum;
        contentLength = src.contentLength;
    }
    
    /**
     * Obtains an average font size in the are in pixels.
     * @return the average font pixel size
     */
    public float getFontSize()
    {
        if (contentLength == 0)
            return 0;
        else
            return fontSizeSum / contentLength;
    }
    
    /**
     * Obtains the average font style. 0 means no text is bold, 1 means all the text is bold.
     * @return a value in the range 0..1
     */
    public float getFontWeight()
    {
        if (contentLength == 0)
            return 0;
        else
            return fontWeightSum / contentLength;
    }
    
    /**
     * Obtains the average font style. 0 means no text in italics, 1 means all the text in italics.
     * @return a value in the range 0..1
     */
    public float getFontStyle()
    {
        if (contentLength == 0)
            return 0;
        else
            return fontStyleSum / contentLength;
    }
    
    /**
     * Obtains the amount of underlined text. 0 means no underlined text, 1 means all the text is underlined.
     * @return a value in the range 0..1
     */
    public float getUnderline()
    {
        if (contentLength == 0)
            return 0;
        else
            return underlineSum / contentLength;
    }
    
    /**
     * Obtains the amount of line-through text. 0 means no underlined text, 1 means all the text is underlined.
     * @return a value in the range 0..1
     */
    public float getLineThrough()
    {
        if (contentLength == 0)
            return 0;
        else
            return lineThroughSum / contentLength;
    }

    /**
     * Returns the sum of all elements the average style is computed from.
     * @return the style sum
     */
    public float getFontSizeSum()
    {
        return fontSizeSum;
    }

    public void setFontSizeSum(float fontSizeSum)
    {
        this.fontSizeSum = fontSizeSum;
    }

    /**
     * Returns the sum of all elements the average style is computed from.
     * @return the style sum
     */
    public float getFontWeightSum()
    {
        return fontWeightSum;
    }

    public void setFontWeightSum(float fontWeightSum)
    {
        this.fontWeightSum = fontWeightSum;
    }

    /**
     * Returns the sum of all elements the average style is computed from.
     * @return the style sum
     */
    public float getFontStyleSum()
    {
        return fontStyleSum;
    }

    public void setFontStyleSum(float fontStyleSum)
    {
        this.fontStyleSum = fontStyleSum;
    }

    /**
     * Returns the sum of all elements the average style is computed from.
     * @return the style sum
     */
    public float getUnderlineSum()
    {
        return underlineSum;
    }

    public void setUnderlineSum(float underlineSum)
    {
        this.underlineSum = underlineSum;
    }

    /**
     * Returns the sum of all elements the average style is computed from.
     * @return the style sum
     */
    public float getLineThroughSum()
    {
        return lineThroughSum;
    }

    public void setLineThroughSum(float lineThroughSum)
    {
        this.lineThroughSum = lineThroughSum;
    }

    /**
     * Returns the total number of elements used for compute the averages from sums.
     * @return the total number of elements.
     */
    public int getContentLength()
    {
        return contentLength;
    }

    public void setContentLength(int contentLength)
    {
        this.contentLength = contentLength;
    }

    /**
     * Updates the average values when a new content rect is added or joined.
     * @param other the other text style
     */
    public void updateAverages(TextStyle other)
    {
        fontSizeSum += other.getFontSizeSum();
        fontWeightSum += other.getFontWeightSum();
        fontStyleSum += other.getFontStyleSum();
        underlineSum += other.getUnderlineSum();
        lineThroughSum += other.getLineThroughSum();
        contentLength += other.getContentLength();
    }
    
}
