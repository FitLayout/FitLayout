package cz.vutbr.fit.layout.bcs.impl;

import java.awt.image.BufferedImage;

import cz.vutbr.fit.layout.model.Color;

public class AverageColor
{
    private Color color;
    private float coloredPortion;

    public AverageColor(Color c, float p)
    {
        this.color = c;
        this.coloredPortion = p;
    }

    public AverageColor(BufferedImage im)
    {
        int x, y;
        int alpha;
        long rSum = 0, gSum = 0, bSum = 0;
        int rgb;
        int count, totalCount;

        count = totalCount = 0;
        if (im == null)
        {
            this.color = null;
            this.coloredPortion = 0;
            return;
        }
        // DOC: how do we count color average
        for (y = 0; y < im.getHeight(); y++)
        {
            for (x = 0; x < im.getWidth(); x++)
            {
                totalCount++;
                rgb = im.getRGB(x, y);
                alpha = (rgb & 0xff000000) >> 24;
                if (alpha == 0) continue;

                rSum += (rgb & 0x00ff0000) >> 16;
                gSum += (rgb & 0x0000ff00) >> 8;
                bSum += (rgb & 0x000000ff);
                count++;
            }
        }

        if (count == 0)
        {
            this.color = null;
            this.coloredPortion = 0;
            return;
        }

        rSum /= count;
        gSum /= count;
        bSum /= count;

        this.color = new Color((int)rSum, (int)gSum, (int)bSum, 255);
        this.coloredPortion = (float)count/totalCount;
    }

    public Color mixWithBackground(Color bg)
    {
        int r = 0, g = 0, b = 0;
        float fgPart = this.coloredPortion;
        float bgPart = 1-fgPart;

        if (bg == null) {
            return new Color(this.color.getRGB());
        }

        r = (int)(this.color.getRed()*fgPart+bg.getRed()*bgPart);
        g = (int)(this.color.getGreen()*fgPart+bg.getGreen()*bgPart);
        b = (int)(this.color.getBlue()*fgPart+bg.getBlue()*bgPart);
        return new Color(r, g, b);
    }

    public Color getColor()
    {
        return this.color;
    }

    public float getColoredPortion()
    {
        return this.coloredPortion;
    }
}
