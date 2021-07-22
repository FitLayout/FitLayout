/**
 * SortByPositionOperator.java
 *
 * Created on 17. 9. 2015, 10:21:22 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;

/**
 * 
 * @author burgetr
 */
public class SortByPositionOperator extends BaseOperator
{
    protected boolean columnFirst;
    
    public SortByPositionOperator()
    {
        columnFirst = false;
    }
    
    public SortByPositionOperator(boolean columnFirst)
    {
        this.columnFirst = columnFirst;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SortByPosition";
    }
    
    @Override
    public String getName()
    {
        return "Sort by position";
    }

    @Override
    public String getDescription()
    {
        return "Sorts the visual areas by their position (x,y coordinates).";
    }

    @Override
    public String getCategory()
    {
        return "Sorting";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterBoolean("columnFirst"));
        return ret;
    }

    public boolean getColumnFirst()
    {
        return columnFirst;
    }

    public void setColumnFirst(boolean columnFirst)
    {
        this.columnFirst = columnFirst;
    }

    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursivelySortChildAreas(root, columnFirst);
    }

    //==============================================================================
    
    protected void recursivelySortChildAreas(Area root, final boolean columnFirst)
    {
        if (root.getChildCount() > 1)
        {
            Vector<Area> list = new Vector<Area>(root.getChildren());
            Collections.sort(list, new Comparator<Area>() {
                public int compare(Area a1, Area a2)
                {
                    if (!columnFirst)
                        return a1.getY1() == a2.getY1() ? a1.getX1() - a2.getX1() : a1.getY1() - a2.getY1();
                    else
                        return a1.getX1() == a2.getX1() ? a1.getY1() - a2.getY1() : a1.getX1() - a2.getX1();
                }
            });
            
            root.removeAllChildren();
            root.appendChildren(list);
        }
        for (int i = 0; i < root.getChildCount(); i++)
            recursivelySortChildAreas(root.getChildAt(i), columnFirst);
        
    }

}
