/**
 * ToolbarLayout.java
 *
 * Created on 24. 1. 2015, 17:28:06 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * A layout manager used for BlockBrowser's toolbar area.
 * @see http://www.java-forums.org/awt-swing/11618-how-control-layout-multiple-toolbars.html
 * @author famousj
 */
public class ToolbarLayout extends FlowLayout
{
    private static final long serialVersionUID = 1L;

    public ToolbarLayout()
    {
        super(FlowLayout.LEADING);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Dimension size = super.minimumLayoutSize(parent);

            int nComponents = parent.getComponentCount();
            if (nComponents > 0)
            {
                int firstY = parent.getComponent(0).getY();
                Component last = parent.getComponent(nComponents - 1);
                int lastY = last.getY();

                if (lastY != firstY)
                {
                    size = new Dimension((int) size.getWidth(), lastY + last.getHeight());
                }
            }

            return size;
        }
    }
}
