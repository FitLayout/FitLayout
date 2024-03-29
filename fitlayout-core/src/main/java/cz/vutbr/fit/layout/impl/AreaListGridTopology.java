/**
 * 
 */
package cz.vutbr.fit.layout.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A grid topology on a generic list of areas.
 * 
 * @author burgetr
 */
public class AreaListGridTopology implements AreaTopology
{
    private Collection<ContentRect> areas;
    private Rectangular abspos;
    private Map<ContentRect, Rectangular> positions;
    private Map<Coords, Set<ContentRect>> index;
    private AreaGrid grid;
    
    public AreaListGridTopology(Collection<ContentRect> areas)
    {
        this(areas, true);
    }
    
    public AreaListGridTopology(Collection<ContentRect> areas, boolean doInit)
    {
        this.areas = areas;
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
        return grid.getWidth();
    }

    @Override
    public int getTopologyHeight()
    {
        return grid.getHeight();
    }

    @Override
    public Rectangular getTopologyPosition()
    {
        return grid.getAbsolutePosition();
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
        final Set<ContentRect> areas = index.get(new Coords(x, y));
        if (areas != null && !areas.isEmpty())
            return areas.iterator().next();
        else
            return null;
    }

    @Override
    public Collection<ContentRect> findAllAreasAt(int x, int y)
    {
        final Set<ContentRect> areas = index.get(new Coords(x, y));
        return (areas == null) ? Collections.emptyList() : areas;
    }

    @Override
    public Collection<ContentRect> findAllAreasIntersecting(Rectangular r)
    {
        Collection<ContentRect> ret = new ArrayList<>();
        for (Map.Entry<ContentRect, Rectangular> entry : positions.entrySet())
        {
            if (entry.getValue().intersects(r))
                ret.add(entry.getKey());
        }
        return ret;
    }
    
    @Override
    public Rectangular toPixelPosition(Rectangular gp)
    {
        return new Rectangular(grid.getColOfs(gp.getX1()),
                grid.getRowOfs(gp.getY1()),
                grid.getColOfs(gp.getX2()+1) - 1,
                grid.getRowOfs(gp.getY2()+1) - 1);
    }
    
    @Override
    public Rectangular toPixelPositionAbsolute(Rectangular gp)
    {
        Rectangular ret = new Rectangular(grid.getColOfs(gp.getX1()),
                grid.getRowOfs(gp.getY1()),
                grid.getColOfs(gp.getX2()+1) - 1,
                grid.getRowOfs(gp.getY2()+1) - 1);
        ret.move(grid.getAbsolutePosition().getX1(), grid.getAbsolutePosition().getY1());
        return ret;
    }
    
    @Override
    public int toTopologyX(int pixelX)
    {
        return grid.findCellX(pixelX);
    }

    @Override
    public int toTopologyY(int pixelY)
    {
        return grid.findCellY(pixelY);
    }

    @Override
    public void update()
    {
        abspos = computeAreaBounds();
        //default positions for all the areas
        positions = new HashMap<>(areas.size());
        for (ContentRect a : areas)
            positions.put(a, new Rectangular());
        //re-create the grid
        grid = new AreaGrid(abspos, areas, this);
        //build the index
        index = new HashMap<>(positions.size());
        for (Map.Entry<ContentRect, Rectangular> entry : positions.entrySet())
            addToIndex(entry.getKey(), entry.getValue());
    }

    @Override
    public void drawLayout(OutputDisplay disp)
    {
        Graphics ig = disp.getGraphics();
        Color c = ig.getColor();
        ig.setColor(Color.BLUE);
        int xo = abspos.getX1();
        for (int i = 1; i <= grid.getWidth(); i++)
        {
            xo += grid.getCols()[i-1];
            ig.drawLine(xo, abspos.getY1(), xo, abspos.getY2());
        }
        int yo = abspos.getY1();
        for (int i = 0; i < grid.getHeight(); i++)
        {
            yo += grid.getRows()[i];
            ig.drawLine(abspos.getX1(), yo, abspos.getX2(), yo);
        }
        ig.setColor(c);
    }

    //=================================================================================
    
    /**
     * Obtains the absolute bounds of the parent area where the child areas are positioned.
     * @return The absloute area bounds in pixels.
     */
    protected Rectangular computeAreaBounds()
    {
        Rectangular ret = null;
        for (ContentRect a : areas)
        {
            if (ret == null)
                ret = new Rectangular(a.getBounds());
            else
                ret.expandToEnclose(a.getBounds());
        }
        return ret;
    }
    
    private void addToIndex(ContentRect a, Rectangular gp)
    {
        for (int x = gp.getX1(); x <= gp.getX2(); x++)
        {
            for (int y = gp.getY1(); y <= gp.getY2(); y++)
            {
                final Coords c = new Coords(x, y);
                Set<ContentRect> careas = index.get(c);
                if (careas == null)
                {
                    careas = new HashSet<>();
                    index.put(c, careas);
                }
                careas.add(a);
            }
        }
    }
    
    //=================================================================================
    
    private static class Coords
    {
        int x;
        int y;
        
        public Coords(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Coords other = (Coords) obj;
            if (x != other.x) return false;
            if (y != other.y) return false;
            return true;
        }
        
    }
    
}
