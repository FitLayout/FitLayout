/**
 * GroupAnalyzerByDOM.java
 *
 * Created on 9. 2. 2016, 14:22:49 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.List;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A group analyzer that groups together the areas that are created by the same DOM element.
 * 
 * @author burgetr
 */
public class GroupAnalyzerByDOM extends GroupAnalyzer
{

    public GroupAnalyzerByDOM(Area parent)
    {
        super(parent);
    }

    @Override
    public Area findSuperArea(Area sub, List<Area> selected)
    {
        String srcId = getId(sub);
        if (srcId != null && !srcId.isEmpty())
        {
            selected.clear();
            Rectangular mingp = null;
            for (int i = 0; i < parent.getChildCount(); i++)
            {
                Area chld = parent.getChildAt(i);
                String cid = getId(chld);
                if (cid != null && !cid.isEmpty() && cid.equals(srcId))
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
            Area area = sub.getAreaTree().createArea(abspos);
            //area.setBorders(true, true, true, true);
            area.setLevel(1);
            //if (!mingp.equals(sub.getGridPosition()))
            //    System.out.println("Found area: " + area + " : " + mingp);
            Area ret = sub.getAreaTree().createArea(area);
            ret.setGridPosition(mingp);
            return ret;
        }
        else
        {
            selected.clear();
            selected.add(sub);
            return sub.getAreaTree().createArea(new Rectangular(0, 0, 0, 0));
        }
    }
    
    private String getId(Area area)
    {
        List<Box> boxes = area.getBoxes();
        if (!boxes.isEmpty())
            return boxes.get(0).getSourceNodeId();
        else
            return null;
    }

}
