/**
 * SortByLinesOperator.java
 *
 * Created on 17. 9. 2015, 13:49:32 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.segm.AreaUtils;

/**
 * 
 * @author burgetr
 */
public class SortByLinesOperator extends SortByPositionOperator
{
    
    public SortByLinesOperator()
    {
        super(false);
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SortByLines";
    }
    
    @Override
    public String getName()
    {
        return "Sort by lines";
    }

    @Override
    public String getDescription()
    {
        return "Sorts the visual areas roughly according to the text lines detected in the file.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return Collections.emptyList();
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
        recursivelySortChildAreas(root, false);
        recursiveSortLines(root);
    }
    
    //==============================================================================
    
    /**
     * Goes through all the areas in the tree and sorts their sub-areas.
     */
    protected void recursiveSortLines(Area root)
    {
        sortChildLines(root);
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveSortLines(root.getChildAt(i));
    }
    
    /**
     * Goes through the grid of areas and sorts the adjacent visual areas that are not
     * separated by anything
     */
    protected void sortChildLines(Area root)
    {
        if (root.getChildCount() > 1)
        {
            List<Area> src = new Vector<Area>(root.getChildren());
            List<Area> dest = new Vector<Area>(src.size());
            while (!src.isEmpty())
            {
                final Area seed = src.get(0);
                List<Area> line = findAreasOnLine(root, seed, src);
                dest.addAll(line);
                src.removeAll(line);
            }
            
            root.removeAllChildren();
            root.appendChildren(dest);
        }
    }

    private List<Area> findAreasOnLine(Area parent, Area area, List<Area> candidates)
    {
        Vector<Area> ret = new Vector<Area>();
        ret.add(area);
        
        final int nx1 = area.getGridPosition().getX1();
        final int ny1 = area.getGridPosition().getY1();
        final int nx2 = area.getGridPosition().getX2();
        final int ny2 = area.getGridPosition().getY2();
        final AreaTopology t = parent.getTopology();
        
        //try to expand to the right
        int dist = 1;
        while (nx2 + dist < t.getTopologyWidth())
        {
            //try to find some node at the right in the given distance
            for (int y = ny1; y <= ny2; y++)
            {
                Area neigh = (Area) t.findAreaAt(nx2 + dist, y);
                if (neigh != null && candidates.contains(neigh)) //something found
                {
                    //the maximal Y difference to consider other areas to be on the same line
                    int threshold = (Math.min(area.getHeight(), neigh.getHeight()) / 2);
                    if (threshold < 0) threshold = 0;
                    //check if the nodes could be on the same line
                    if (AreaUtils.isOnSameLine(area, neigh, threshold))
                    {
                        ret.add(neigh);
                        break;
                    }
                }
            }
            dist++;
        }
        //try to expand to the left
        dist = 1;
        while (nx1 - dist >= 0)
        {
            //try to find some node at the right in the given distance
            for (int y = ny1; y <= ny2; y++)
            {
                Area neigh = (Area) t.findAreaAt(nx1 - dist, y);
                if (neigh != null && candidates.contains(neigh)) //something found
                {
                    //the maximal Y difference to consider other areas to be on the same line
                    int threshold = (Math.min(area.getHeight(), neigh.getHeight()) / 2);
                    if (threshold < 0) threshold = 0;
                    //check if the nodes could be on the same line
                    if (AreaUtils.isOnSameLine(area, neigh, threshold))
                    {
                        ret.insertElementAt(neigh, 0);
                        break;
                    }
                }
            }
            dist++;
        }
            
        return ret;
    }
    
}
