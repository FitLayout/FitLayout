/**
 * BoxImpl.java
 *
 * Created on 6. 11. 2020, 10:40:37 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;

/**
 * A standard box implementation extended by additional properties necessary for building
 * the resulting tree correctly.
 * @author burgetr
 */
public class BoxImpl extends DefaultBox
{
    private BoxList parentList;
    private BoxImpl offsetParent;
    private BoxImpl domParent;
    
    private TextStyle intrinsicTextStyle;
    private boolean absolute; //position: absolute
    private boolean fixed; //position: fixed
    private boolean clipping; //does it clip the contents
    

    public BoxImpl(BoxList parentList)
    {
        super();
        this.parentList = parentList; 
        absolute = false;
        intrinsicTextStyle = new TextStyle();
    }

    public BoxList getParentList()
    {
        return parentList;
    }

    public BoxImpl getOffsetParent()
    {
        return offsetParent;
    }

    public void setOffsetParent(BoxImpl offsetParent)
    {
        this.offsetParent = offsetParent;
    }

    public BoxImpl getDomParent()
    {
        return domParent;
    }

    public void setDomParent(BoxImpl domParent)
    {
        this.domParent = domParent;
    }

    public boolean isAbsolute()
    {
        return absolute;
    }

    public void setAbsolute(boolean absolute)
    {
        this.absolute = absolute;
    }

    public boolean isFixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }

    public boolean isClipping()
    {
        return clipping;
    }

    public void setClipping(boolean clipping)
    {
        this.clipping = clipping;
    }

    public TextStyle getIntrinsicTextStyle()
    {
        return intrinsicTextStyle;
    }

    public void setIntrinsicTextStyle(TextStyle intrinsicTextStyle)
    {
        this.intrinsicTextStyle = intrinsicTextStyle;
    }
    
    /**
     * Applies the intrinsic bounds as the initial content bounds.
     */
    public void applyIntrinsicBounds()
    {
        setBounds(new Rectangular(getIntrinsicBounds()));
        setContentBounds(new Rectangular(getIntrinsicBounds()));
    }
    
    /**
     * Finds the nearest ancestor that may clip the contents of this box (it has the 'overflow'
     * value different from 'visible');
     * @return an ancestor box used for clipping or {@code null} when there is no such box
     */
    public BoxImpl getClipBox()
    {
        BoxImpl parent = (BoxImpl) getIntrinsicParent();
        while (parent != null)
        {
            if (parent.isClipping())
                return parent;
            parent = (BoxImpl) parent.getIntrinsicParent();
        }
        return null;
    }
    
}
