/**
 * Color.java
 *
 * Created on 22. 3. 2020, 15:16:42 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * An RGBA color.
 */
public class Color
{
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    
    private final int value;
    

    /**
     * Creates a color from the RGB components.
     * @param red
     * @param green
     * @param blue
     */
    public Color(final int red, final int green, final int blue)
    {
        this(red, green, blue, 255);
    }

    /**
     * Creates a color from the RGBA components.
     * @param red
     * @param green
     * @param blue
     */
    public Color(final int red, final int green, final int blue, final int alpha)
    {
        this.value = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8) | ((blue & 0xFF));
    }
    
    /**
     * Creates a color from an RGB value
     * @param rgb the RGB value
     */
    public Color(int rgb)
    {
        this.value = rgb;
    }

    /**
     * Returns the RGB value representing the color.
     */
    public int getRGB()
    {
        return value;
    }

    /**
     * Returns the red value in the range 0-255.
     *
     * @return the red value.
     */
    public int getRed()
    {
        return (value >> 16) & 0xFF;
    }

    /**
     * Returns the green value in the range 0-255.
     *
     * @return the green value.
     */
    public int getGreen()
    {
        return (value >> 8) & 0xFF;
    }

    /**
     * Returns the blue value in the range 0-255.
     *
     * @return the blue component.
     */
    public int getBlue()
    {
        return (value) & 0xFF;
    }

    /**
     * Returns the alpha value in the range 0-255.
     *
     * @return the alpha value.
     */
    public int getAlpha()
    {
        return (value >> 24) & 0xff;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Color color = (Color) o;

        return value == color.value;
    }

    @Override
    public int hashCode()
    {
        return value;
    }

}
