/**
 * BrowserTabBase.java
 *
 * Created on 22. 4. 2020, 20:32:50 by burgetr
 */
package cz.vutbr.fit.layout.tools.gui;

import cz.vutbr.fit.layout.tools.BlockBrowser;

/**
 * 
 * @author burgetr
 */
public abstract class BrowserTabBase implements BrowserTab
{
    protected BlockBrowser browser;
    private boolean active;

    
    public BrowserTabBase(BlockBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isActive()
    {
        return active;
    }

    @Override
    public void setActive(boolean active)
    {
        this.active = active;
    }

}
