/**
 * ContentImage.java
 *
 * Created on 29. 10. 2014, 11:19:39 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.net.URL;

/**
 * An image used as the box content.
 * 
 * @author burgetr
 */
public interface ContentImage extends ContentObject
{

    /**
     * Obtains the image URL.
     * @return The URL of the image or {@code null} when the image has no URL.
     */
    public URL getUrl();
    
}
