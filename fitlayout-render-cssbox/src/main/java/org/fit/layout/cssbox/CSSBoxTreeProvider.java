/**
 * CSSBoxTreeProvider.java
 *
 * Created on 27. 1. 2015, 15:14:55 by burgetr
 */
package org.fit.layout.cssbox;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.layout.Viewport;
import org.fit.layout.api.Parameter;
import org.fit.layout.impl.BaseBoxTreeProvider;
import org.fit.layout.impl.ParameterBoolean;
import org.fit.layout.impl.ParameterFloat;
import org.fit.layout.impl.ParameterInt;
import org.fit.layout.impl.ParameterString;
import org.fit.layout.model.Page;
import org.xml.sax.SAXException;

/**
 * A box tree provider implementation based on CSSBox 
 * 
 * @author burgetr
 */
public class CSSBoxTreeProvider extends BaseBoxTreeProvider
{
    private String urlstring;
    private int width;
    private int height;
    private float zoom;
    private boolean useVisualBounds;
    private boolean preserveAux;
    private boolean replaceImagesWithAlt; //not published as a parameter now
    
    private CSSBoxTreeBuilder builder;
    
    public CSSBoxTreeProvider()
    {
        urlstring = null;
        width = 1200;
        height = 800;
        zoom = 1.0f;
        useVisualBounds = true;
        preserveAux = false;
    }
    
    public CSSBoxTreeProvider(URL url, int width, int height, float zoom, boolean useVisualBounds, boolean preserveAux)
    {
        this.urlstring = url.toString();
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.useVisualBounds = useVisualBounds;
        this.preserveAux = preserveAux;
    }

    @Override
    public String getId()
    {
        return "FitLayout.CSSBox";
    }

    @Override
    public String getName()
    {
        return "CSSBox HTML and PDF renderer";
    }

    @Override
    public String getDescription()
    {
        return "Uses the CSSBox rendering engine for obtaining the box tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(5);
        ret.add(new ParameterString("url", 0, 64));
        ret.add(new ParameterInt("width", 10, 9999));
        ret.add(new ParameterInt("height", 10, 9999));
        ret.add(new ParameterFloat("zoom", -5.0f, 5.0f));
        ret.add(new ParameterBoolean("useVisualBounds"));
        ret.add(new ParameterBoolean("preserveAux"));
        return ret;
    }
    
    public String getUrl()
    {
        return urlstring;
    }

    public void setUrl(String url)
    {
        urlstring = new String(url);
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }
    
    public float getZoom()
    {
        return zoom;
    }

    public void setZoom(float zoom)
    {
        this.zoom = zoom;
    }

    public boolean getUseVisualBounds()
    {
        return useVisualBounds;
    }

    public void setUseVisualBounds(boolean useVisualBounds)
    {
        this.useVisualBounds = useVisualBounds;
    }

    public boolean getPreserveAux()
    {
        return preserveAux;
    }

    public void setPreserveAux(boolean preserveAux)
    {
        this.preserveAux = preserveAux;
    }

    public boolean getReplaceImagesWithAlt()
    {
        return replaceImagesWithAlt;
    }

    public void setReplaceImagesWithAlt(boolean replaceImagesWithAlt)
    {
        this.replaceImagesWithAlt = replaceImagesWithAlt;
    }

    @Override
    public Page getPage()
    {
        builder = new CSSBoxTreeBuilder(new Dimension(width, height), useVisualBounds, preserveAux, replaceImagesWithAlt);
        builder.setZoom(zoom);
        try {
            builder.parse(urlstring);
            return builder.getPage();
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Viewport getViewport()
    {
        if (builder != null)
            return builder.getViewport();
        else
            return null;
    }

}
