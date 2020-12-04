/**
 * DefaultContentImage.java
 *
 * Created on 4. 12. 2020, 21:56:59 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.net.URL;

import cz.vutbr.fit.layout.model.ContentImage;

/**
 * 
 * @author burgetr
 */
public class DefaultContentImage implements ContentImage
{
    private URL url;
    private byte[] pngData;

    @Override
    public URL getUrl()
    {
        return url;
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }

    @Override
    public byte[] getPngData()
    {
        return pngData;
    }

    public void setPngData(byte[] pngData)
    {
        this.pngData = pngData;
    }

}
