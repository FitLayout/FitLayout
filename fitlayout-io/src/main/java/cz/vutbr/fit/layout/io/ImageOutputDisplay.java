/**
 * ImageOutputDisplay.java
 *
 * Created on 16. 11. 2016, 9:53:03 by burgetr
 */
package cz.vutbr.fit.layout.io;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * 
 * @author burgetr
 */
public class ImageOutputDisplay extends Graphics2DDisplay
{
    private BufferedImage image;
    

    public ImageOutputDisplay(int width, int height)
    {
        super(null);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setGraphics(image.createGraphics());
    }
    
    public void saveTo(String path) throws IOException
    {
        FileOutputStream os = new FileOutputStream(path);
        saveTo(os);
        os.close();
    }
    
    public void saveTo(OutputStream os) throws IOException
    {
        ImageIO.write(image, "png", os);
    }
    
}
