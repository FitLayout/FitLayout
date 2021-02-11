/**
 * ContentImageImpl.java
 *
 * Created on 29. 10. 2014, 11:21:03 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.fit.cssbox.awt.BitmapImage;
import org.fit.cssbox.layout.ReplacedImage;

import cz.vutbr.fit.layout.model.ContentImage;

/**
 * 
 * @author burgetr
 */
public class ContentImageImpl implements ContentImage
{
    private ReplacedImage image;
    
    
    public ContentImageImpl(ReplacedImage image)
    {
        this.image = image;
    }

    public ReplacedImage getImage()
    {
        return image;
    }

    public void setImage(ReplacedImage image)
    {
        this.image = image;
    }

    @Override
    public URL getUrl()
    {
        if (image != null)
            return image.getUrl();
        else
            return null;
    }

    @Override
    public byte[] getPngData()
    {
        if (image != null)
        {
            org.fit.cssbox.layout.ContentImage img = image.getImage();
            if (img != null && img instanceof BitmapImage)
            {
                BufferedImage bimg = ((BitmapImage) img).getBufferedImage();
                if (bimg != null)
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try
                    {
                        ImageIO.write(bimg, "png", baos);
                        return baos.toByteArray();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

}
