/**
 * AreaImpl.java
 *
 * Created on 23. 10. 2020, 9:02:19 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.List;

import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class AreaImpl extends DefaultArea
{

    public AreaImpl(Box box)
    {
        super(box);
    }

    public AreaImpl(DefaultArea src)
    {
        super(src);
    }

    public AreaImpl(int x1, int y1, int x2, int y2)
    {
        super(x1, y1, x2, y2);
    }

    public AreaImpl(List<Box> boxList)
    {
        super(boxList);
    }

    public AreaImpl(Rectangular r)
    {
        super(r);
    }

}
