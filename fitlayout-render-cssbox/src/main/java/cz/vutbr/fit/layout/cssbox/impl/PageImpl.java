/**
 * PageImpl.java
 *
 * Created on 22. 10. 2014, 14:25:28 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.net.URL;

import cz.vutbr.fit.layout.impl.DefaultPage;

/**
 * 
 * @author burgetr
 */
public class PageImpl extends DefaultPage
{

    public PageImpl(URL url)
    {
        super(url);
    }

    @Override
    public String getLabel()
    {
        if (super.getLabel() != null)
            return super.getLabel();
        else if (getTitle() != null)
            return getTitle();
        else if (getSourceURL() != null)
            return getSourceURL().toString();
        else
            return null;
    }
    
}
