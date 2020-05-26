/**
 * PageImpl.java
 *
 * Created on 22. 10. 2014, 14:25:28 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.net.URL;
import java.util.Vector;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * Default generic page implementation.
 * 
 * @author burgetr
 */
public class DefaultPage extends BaseArtifact implements Page
{
    protected URL url;
    protected String title;
    protected Box root;
    protected int width;
    protected int height;
    

    public DefaultPage(URL url)
    {
        super(null);
        this.url = url;
    }
    
    public DefaultPage(Page src)
    {
        super(null);
        url = src.getSourceURL();
        title = new String(src.getTitle());
        root = src.getRoot();
        width = src.getWidth();
        height = src.getHeight();
    }
    
    @Override
    public IRI getArtifactType()
    {
        return BOX.Page;
    }

    @Override
    public URL getSourceURL()
    {
        return url;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public Box getRoot()
    {
        return root;
    }

    public void setRoot(Box root)
    {
        this.root = root;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    @Override
    public Box getBoxAt(int x, int y)
    {
        return recursiveGetBoxAt(root, x, y);
    }
    
    protected Box recursiveGetBoxAt(Box root, int x, int y)
    {
        if (root.getBounds().contains(x, y))
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                Box ret = recursiveGetBoxAt(root.getChildAt(i), x, y);
                if (ret != null)
                    return ret;
            }
            return root;
        }
        else
            return null;
    }

    @Override
    public Vector<Box> getBoxesInRegion(Rectangular r)
    {
        Vector<Box> ret = new Vector<Box>();
        recursiveGetBoxesInRegion(root, r, ret);
        return ret;
    }
    
    private void recursiveGetBoxesInRegion(Box root, Rectangular r, Vector<Box> result)
    {
        if (r.encloses(root.getVisualBounds()))
        {
            if (!root.getVisualBounds().isEmpty())
                result.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                recursiveGetBoxesInRegion(root.getChildAt(i), r, result);
        }
    }

    @Override
    public String toString()
    {
        String ret = "";
        if (getSourceURL() != null)
            ret += getSourceURL().toString();
        else
            ret += "- unknown URL -";
        
        ret += " [" + getId() + "]";
        
        return ret;
    }
    
}
