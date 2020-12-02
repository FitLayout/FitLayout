/**
 * PageImpl.java
 *
 * Created on 11. 11. 2020, 8:57:57 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

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
