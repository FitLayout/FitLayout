/**
 * ImageOutput.java
 *
 * Created on 11. 12. 2020, 20:49:22 by burgetr
 */
package cz.vutbr.fit.layout.bcs.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 
 * @author burgetr
 */
public class ImageOutput
{
    private List<PageArea> areas;
    private int width;
    private int height;
    
    public ImageOutput(List<PageArea> areas, int width, int height)
    {
        this.areas = areas;
        this.width = width;
        this.height = height;
    }
    
    public void save(String path)
    {
        BufferedImage img = createImage();
        File outfile = new File(path);
        try {
            ImageIO.write(img, "png", outfile);
        } catch (IOException e) {
        }
    }
    
    private BufferedImage createImage()
    {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        for (PageArea area : areas)
        {
            g.setColor(toAWTColor(area.getColor()));
            g.fillRect(area.getLeft(), area.getTop(),
                    area.getRight() - area.getLeft() + 1, area.getBottom() - area.getTop() + 1);
        }
        
        return img;
    }
    
    private Color toAWTColor(cz.vutbr.fit.layout.model.Color src)
    {
        if (src == null)
            return null;
        else
            return new Color(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
    }


}
