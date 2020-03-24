/**
 * ContentImageImpl.java
 *
 * Created on 29. 10. 2014, 11:21:03 by burgetr
 */
package org.fit.layout.cssbox;

import java.net.URL;

import org.fit.cssbox.layout.ReplacedImage;
import org.fit.layout.model.ContentImage;

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

}
