/**
 * AreaListPixelTopology.java
 *
 * Created on 24. 10. 2023, 8:57:59 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import net.sf.jsi.Area;
import net.sf.jsi.AreaCallback;
import net.sf.jsi.RTree;
import net.sf.jsi.Spot;

/**
 * A topology that uses directly the pixel coordinates of the rectangles.
 * 
 * @author burgetr
 */
public class AreaListPixelTopology implements AreaTopology
{
    private List<ContentRect> areas;
    private Rectangular abspos;
    private Map<ContentRect, Rectangular> positions;
    private RTree areaRTree;
    
    
    public AreaListPixelTopology(List<ContentRect> areas, Rectangular abspos)
    {
        this(areas, abspos, true);
    }
    
    public AreaListPixelTopology(List<ContentRect> areas, Rectangular abspos, boolean doInit)
    {
        this.areas = areas;
        this.abspos = abspos;
        //create the grid if required
        if (doInit)
            update();
    }

    @Override
    public Collection<ContentRect> getAreas()
    {
        return areas;
    }

    public void setAreas(List<ContentRect> areas)
    {
        this.areas = areas;
    }
    
    @Override
    public int getTopologyWidth()
    {
        return abspos.getWidth();
    }

    @Override
    public int getTopologyHeight()
    {
        return abspos.getHeight();
    }

    @Override
    public Rectangular getTopologyPosition()
    {
        return abspos;
    }

    @Override
    public Rectangular getPosition(ContentRect area)
    {
        return positions.get(area);
    }

    @Override
    public void setPosition(ContentRect area, Rectangular gp)
    {
        positions.put(area, gp);
    }

    @Override
    public Map<ContentRect, Rectangular> getPositionMap()
    {
        return positions;
    }

    @Override
    public ContentRect findAreaAt(int x, int y)
    {
        final AreaMatch match = new AreaMatch(false);
        areaRTree.nearest(new Spot(x, y), match, 0.05f);
        if (!match.getRects().isEmpty())
            return match.getRects().get(0);
        else
            return null;
    }

    @Override
    public Collection<ContentRect> findAllAreasAt(int x, int y)
    {
        final AreaMatch match = new AreaMatch(true);
        areaRTree.nearest(new Spot(x, y), match, 0.05f);
        return match.getRects();
    }

    @Override
    public Collection<ContentRect> findAllAreasIntersecting(Rectangular r)
    {
        final Area query = new Area(r.getX1(), r.getY1(), r.getX2(), r.getY2());
        final AreaMatch match = new AreaMatch(true);
        areaRTree.intersects(query, match);
        return match.getRects();
    }
    
    @Override
    public Rectangular toPixelPosition(Rectangular pos)
    {
        // the positions are already in pixels
        return pos;
    }
    
    @Override
    public Rectangular toPixelPositionAbsolute(Rectangular gp)
    {
        final Rectangular ret = new Rectangular(gp);
        ret.move(abspos.getX1(), abspos.getY1());
        return ret;
    }
    
    @Override
    public int toTopologyX(int pixelX)
    {
        return pixelX;
    }

    @Override
    public int toTopologyY(int pixelY)
    {
        return pixelY;
    }

    @Override
    public void update()
    {
        //default positions for all the areas
        positions = new HashMap<>(areas.size());
        for (ContentRect a : areas)
            positions.put(a, new Rectangular());
        //build the index
        updateIndex();
    }

    @Override
    public void drawLayout(OutputDisplay disp)
    {
    }

    protected void updateIndex()
    {
        areaRTree = new RTree();
        for (int i = 0; i < areas.size(); i++)
        {
            final ContentRect rect = areas.get(i);
            final Area area = new Area(rect.getX1(), rect.getY1(), rect.getX2(), rect.getY2());
            areaRTree.add(area, i);
        }
    }
    
    //=========================================================================

    class AreaMatch implements AreaCallback
    {
        private final List<ContentRect> rects;
        private final boolean processAll;

        public AreaMatch(boolean processAll)
        {
            this.rects = new ArrayList<>();
            this.processAll = processAll;
        }

        @Override
        public boolean processArea(int id)
        {
            rects.add(areas.get(id));
            return processAll;
        }

        public List<ContentRect> getRects() 
        {
            return rects;
        }
    };

}
