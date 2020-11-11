/**
 * BoxImpl.java
 *
 * Created on 6. 11. 2020, 10:40:37 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.TextStyle;

/**
 * 
 * @author burgetr
 */
public class BoxImpl extends DefaultBox
{
    private TextStyle intrinsicTextStyle;

    public BoxImpl()
    {
        super();
        intrinsicTextStyle = new TextStyle();
    }

    public TextStyle getIntrinsicTextStyle()
    {
        return intrinsicTextStyle;
    }

    public void setIntrinsicTextStyle(TextStyle intrinsicTextStyle)
    {
        this.intrinsicTextStyle = intrinsicTextStyle;
    }
    
}
