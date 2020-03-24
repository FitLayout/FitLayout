/**
 * GridTopology.java
 *
 * Created on 12. 11. 2014, 10:33:00 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.List;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * The default topology that creates a grid of child areas within a parent area.
 * 
 * @author burgetr
 */
public class DefaultGridTopology extends AreaListGridTopology
{
    private Area area;
    private boolean dirty;
    
    public DefaultGridTopology(Area area)
    {
        super(area.getChildren(), false);
        this.area = area;
        update();
    }

    public Area getArea()
    {
        return area;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    @Override
    public void setAreas(List<Area> areas)
    {
        if (getAreas() != areas)
            dirty = true;
        super.setAreas(areas);
    }

    @Override
    public Rectangular getPosition(Area area)
    {
        checkDirty();
        return super.getPosition(area);
    }

    @Override
    public Area findAreaAt(int x, int y)
    {
        checkDirty();
        return super.findAreaAt(x, y);
    }

    @Override
    public Rectangular toPixelPosition(Rectangular gp)
    {
        checkDirty();
        return super.toPixelPosition(gp);
    }

    @Override
    public Rectangular toPixelPositionAbsolute(Rectangular gp)
    {
        checkDirty();
        return super.toPixelPositionAbsolute(gp);
    }

    @Override
    public void drawLayout(OutputDisplay disp)
    {
        checkDirty();
        super.drawLayout(disp);
    }

    @Override
    public void update()
    {
        setAreas(area.getChildren());
        dirty = false;
        super.update();
    }
    
    @Override
    protected Rectangular computeAreaBounds()
    {
        return area.getBounds();
    }

    private void checkDirty()
    {
        if (dirty)
            update();
    }

}
