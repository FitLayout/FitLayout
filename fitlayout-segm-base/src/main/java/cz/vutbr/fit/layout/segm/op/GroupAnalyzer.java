/**
 * GroupAnalyzer.java
 *
 * Created on 23.1.2007, 14:16:41 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.List;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.segm.AreaImpl;

/**
 * A general analyzer to find area groups
 *  
 * @author burgetr
 */
public class GroupAnalyzer
{
    protected Area parent;
    
    public GroupAnalyzer(Area parent)
    {
        this.parent = parent;
    }
    
    public Area getParent()
    {
        return parent;
    }
    
    public AreaTopology getTopology()
    {
        return parent.getTopology();
    }
    
    /**
     * Starts with a specified subarea and finds all the subareas that
     * may be joined with the first one. Returns an empty area and the vector
     * of the areas inside. The subareas are not automatically added to the
     * new area because this would cause their removal from the parent area.
     * @param sub the subnode to start with
     * @param selected a vector that will be filled with the selected subnodes 
     * that should be contained in the new area
     * @return the new empty area
     */
    public Area findSuperArea(Area sub, List<Area> selected)
    {
    	/* This is a simple testing SuperArea implementation. It groups each 
    	 * subarea with its first sibling area.*/
        Area ret = sub.getAreaTree().createArea(new Rectangular(0, 0, 0, 0));
        AreaImpl sibl = (AreaImpl) sub.getNextSibling();
        selected.clear();
        selected.add(sub);
        if (sibl != null)
        {
            selected.add(sibl);
        }
        return ret;
    }
    
    //===================================================================================
    
    /*protected Color debugColor = new Color(0, 0, 255, 20);
    
    protected void dispCell(int x, int y)
    {
        Rectangular bnd = grid.getCellBoundsAbsolute(x, y);
        BrowserCanvas canv = BlockBrowser.browser.getBrowserCanvas();
        java.awt.Graphics g = canv.getImageGraphics();
        g.setColor(debugColor);
        g.fillRect(bnd.getX1(), bnd.getY1(), bnd.getWidth(), bnd.getHeight());
        //System.out.println("Bnd: " + bnd);
        canv.update(canv.getGraphics());
    }

    protected void dispArea(Rectangular area)
    {
        BrowserCanvas canv = BlockBrowser.browser.getBrowserCanvas();
        java.awt.Graphics g = canv.getImageGraphics();
        g.setColor(debugColor);
        for (int y = area.getY1(); y <= area.getY2(); y++)
            for (int x = area.getX1(); x <= area.getX2(); x++)
            {
                Rectangular bnd = grid.getCellBoundsAbsolute(x, y);
                g.fillRect(bnd.getX1(), bnd.getY1(), bnd.getWidth(), bnd.getHeight());
            }
        canv.update(canv.getGraphics());
    }
    
    protected void wait(int ms)
    {
        //System.out.println("waiting");
        try {
            Thread.sleep(ms);
        } catch(InterruptedException e) {}
    }*/

    
}
