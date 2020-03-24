/**
 * GroupAnalyzerByDOM.java
 *
 * Created on 9. 2. 2016, 14:22:49 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.Vector;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.segm.AreaImpl;

/**
 * A group analyzer that groups together the areas that are created by the same DOM element.
 * 
 * @author burgetr
 */
public class GroupAnalyzerByDOM extends GroupAnalyzer
{

    public GroupAnalyzerByDOM(AreaImpl parent)
    {
        super(parent);
    }

    @Override
    public AreaImpl findSuperArea(AreaImpl sub, Vector<AreaImpl> selected)
    {
        Integer srcId = getId(sub);
        if (srcId != null)
        {
            selected.removeAllElements();
            Rectangular mingp = null;
            for (int i = 0; i < parent.getChildCount(); i++)
            {
                AreaImpl chld = (AreaImpl) parent.getChildAt(i);
                Integer cid = getId(chld);
                if (cid != null && cid.equals(srcId))
                {
                    selected.add(chld);
                    if (mingp == null)
                        mingp = new Rectangular(chld.getGridPosition());
                    else
                        mingp.expandToEnclose(chld.getGridPosition());
                }
            }
            
            //create the new area
            Rectangular abspos = getTopology().toPixelPosition(mingp);
            abspos.move(parent.getX1(), parent.getY1());
            AreaImpl area = new AreaImpl(abspos);
            area.setPage(sub.getPage());
            //area.setBorders(true, true, true, true);
            area.setLevel(1);
            //if (!mingp.equals(sub.getGridPosition()))
            //    System.out.println("Found area: " + area + " : " + mingp);
            AreaImpl ret = new AreaImpl(area);
            ret.setGridPosition(mingp);
            return ret;
        }
        else
        {
            selected.removeAllElements();
            selected.add(sub);
            return new AreaImpl(0, 0, 0, 0);
        }
    }
    
    private Integer getId(Area area)
    {
        Vector<Box> boxes = area.getBoxes();
        if (!boxes.isEmpty())
            return boxes.get(0).getSourceNodeId();
        else
            return null;
    }

}
