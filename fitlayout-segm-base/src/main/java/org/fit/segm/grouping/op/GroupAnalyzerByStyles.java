/**
 * GroupAnalyzerByStyles.java
 *
 * Created on 23.1.2007, 15:49:52 by burgetr
 */
package org.fit.segm.grouping.op;

import java.util.Iterator;
import java.util.Vector;

import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.segm.grouping.AreaImpl;

/**
 * This group analyzer tries to expand the selected box to all directions stopping on 
 * whitespace separators. Only the areas with the same font properties are connected.
 * 
 * @author burgetr
 */
public class GroupAnalyzerByStyles extends GroupAnalyzer
{
    private static final short DIR_DOWN = 0;
    private static final short DIR_UP = 1;
    private static final short DIR_RIGHT = 2;
    private static final short DIR_LEFT = 3;
    
    private static final short REQ_BOTH = 0;
    private static final short REQ_HORIZONTAL = 1;
    private static final short REQ_VERTICAL = 2;
    
    private SeparatorSet seps = null;
    
    /**
     * The maximal level of areas we're connecting. The levels used by this analyzer are:
     * 0=basic areas formed by boxes
     * 1=artificial areas
     */
    private int maxlevel;
    
    /** Compare styles while expanding? */
    private boolean matchstyles;
    
    //======================================================================================

    /**
     * Creates a new area analyzer.
     * @param parent the area processed by the analyzer
     * @param maxlevel maximal level of the areas that can be joined into a super area
     * @param matchstyles defines whether to compare the element styles while expanding
     */
    public GroupAnalyzerByStyles(AreaImpl parent, int maxlevel, boolean matchstyles)
    {
        super(parent);
        this.maxlevel = maxlevel;
        this.matchstyles = matchstyles;
    }

    @Override
    public AreaImpl findSuperArea(AreaImpl sub, Vector<AreaImpl> selected)
    {
        //parent.createSeparators();
        seps = parent.getSeparators();
        AreaTopology t = parent.getTopology();
        
        //starting grid position
        Rectangular gp = new Rectangular(sub.getGridPosition());
        System.out.println("GSS************* Start: " + gp + " - " + sub);
        
        //try to expand to the whole grid
        Rectangular limit = new Rectangular(0, 0, getTopology().getTopologyWidth()-1, getTopology().getTopologyHeight()-1);
        expandToLimit(sub, gp, limit, sub, true, true, DIR_RIGHT, REQ_BOTH);
        
        //select areas inside of the area found
        selected.removeAllElements();
        Rectangular mingp = null;
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            final AreaImpl chld = (AreaImpl) parent.getChildAt(i);
            final Rectangular cgp = t.getPosition(chld);
            if (gp.encloses(cgp))
            {
                selected.add(chld);
                if (mingp == null)
                    mingp = new Rectangular(cgp);
                else
                    mingp.expandToEnclose(cgp);
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
        area.setGridPosition(mingp);
        return area;
    }
    
    /**
     * Tries to expand the area in the grid to a greater rectangle in the given limits.
     * @param sub the area to be expanded
     * @param gp the initial grid position of the area. This structure is modified by the expansion.
     * @param limit the maximal size to expand to
     * @param hsep stop on horizontal separators
     * @param vsep stop on vertical separators
     * @param prefDir preferred expansion direction (use DIR_DOWN for vertical
     *  or DIR_RIGHT for horizontal)
     * @param required indicates whether it is required to reach the specified limit horizontaly
     * 	vertically or in both directions (use REQ_* constants) 
     */
    private void expandToLimit(AreaImpl sub, Rectangular gp, Rectangular limit, AreaImpl template, 
    							boolean hsep, boolean vsep,
    							short prefDir, short required)
    {
    	//System.out.println();
    	System.out.println("  Expand " + sub + " DIR=" + prefDir + " sep=" + hsep + ":" + vsep);
    	//debugColor = new java.awt.Color(debugColor.getBlue(), debugColor.getRed(), debugColor.getGreen());*/
        //hsep = true;
        //vsep = true;
        if (getTopology().getTopologyWidth() > 0 && getTopology().getTopologyHeight() > 0 && !sub.isBackgroundSeparated())
        {
            int dir = prefDir;
            int attempts = 0;
            while (attempts < 4 && !limitReached(gp, limit, required))
            {
                boolean change = false;
                int newx, newy;
                //System.out.println(dir + " - " +gp);
                
                switch (dir)
                {
                    case DIR_DOWN:
                        //expand down
                        if (gp.getY2() < limit.getY2() && 
                            (!hsep || !separatorDown(sub.getGridPosition().replaceY(gp)))) //look for the separator under the current expanded bounds
                        {
                            newy = expandVertically(gp, limit, template, true, hsep);
                            if (newy > gp.getY2())
                            {
                                gp.setY2(newy);
                                change = true;
                            }
                        }
                        break;
                        
                    case DIR_RIGHT:
                        //expand right
                        if (gp.getX2() < limit.getX2() &&
                            (!vsep || !separatorRight(sub.getGridPosition().replaceX(gp))))
                        {
                            newx = expandHorizontally(gp, limit, template, true, vsep);
                            if (newx > gp.getX2())
                            {
                                gp.setX2(newx);
                                change = true;
                            }
                        }
                        break;
                        
                    case DIR_UP:
                        //expand up
                        if (gp.getY1() > limit.getY1() &&
                            (!hsep || !separatorUp(sub.getGridPosition().replaceY(gp))))
                        {
                            newy = expandVertically(gp, limit, template, false, hsep);
                            if (newy < gp.getY1())
                            {
                                gp.setY1(newy);
                                change = true;
                            }
                        }
                        break;
                        
                    case DIR_LEFT:
                        //expand left
                        if (gp.getX1() > limit.getX1() && 
                            (!vsep || !separatorLeft(sub.getGridPosition().replaceX(gp))))
                        {
                            newx = expandHorizontally(gp, limit, template, false, vsep);
                            if (newx < gp.getX1())
                            {
                                gp.setX1(newx);
                                change = true;
                            }
                        }
                        break;
                }
                
                if (!change) //not succeeded in this direction 
                {
                    dir++; //another direction
                    if (dir >= 4) dir = 0;
                    attempts++;
                }
                else //succeeded - keep the direction
                {
                    attempts = 0;
                }
                
                /*if (Config.DEBUG_AREAS)
                {
                    dispArea(gp);
                    wait(Config.DEBUG_DELAY);
                }*/
            }
        }
    }
   
    /**
     * Checks if the grid bounds have reached a specified limit in the specified direction.
     * @param gp the bounds to check
     * @param limit the limit to be reached
     * @param required the required direction (use the REQ_* constants)
     * @return true if the limit has been reached or exceeded
     */
    private boolean limitReached(Rectangular gp, Rectangular limit, short required)
    {
    	switch (required)
    	{
    		case REQ_HORIZONTAL:
    			return gp.getX1() <= limit.getX1() && gp.getX2() >= limit.getX2();
    		case REQ_VERTICAL:
    			return gp.getY1() <= limit.getY1() && gp.getY2() >= limit.getY2();
    		case REQ_BOTH:
    			return gp.getX1() <= limit.getX1() && gp.getX2() >= limit.getX2()
    			    && gp.getY1() <= limit.getY1() && gp.getY2() >= limit.getY2();
    			
    	}
    	return false;
    }
    
    
    /**
     * Try to expand the area vertically by a smallest step possible
     * @param gp the area position in the grid
     * @param limit the maximal expansion limit
     * @param down <code>true</code> meand expand down, <code>false<code> means expand up
     * @param sep stop on separators
     * @return the new vertical end of the area.
     */ 
    private int expandVertically(Rectangular gp, Rectangular limit, AreaImpl template, boolean down, boolean sep)
    {
        //System.out.println("exp: " + gp + (down?" _":" ^") + " " + sep);
        int na = down ? gp.getY2() : gp.getY1(); //what to return when it's not possible to expand
        int targety = down ? (gp.getY2() + 1) : (gp.getY1() - 1); 
        //find candidate boxes
        Vector<AreaImpl> cands = new Vector<AreaImpl>();
        int x = gp.getX1();
        while (x <= gp.getX2()) //scan everything at the target position
        {
            AreaImpl cand = (AreaImpl) getTopology().findAreaAt(x, targety);
            //ignore candidates that intersect with our area (could leat to an infinite loop)
            if (cand == null || cand.getGridPosition().intersects(gp))
                x++;
            else
            {
                cands.add(cand);
                x = cand.getGridPosition().getX2() + 1;
            }
        }
        //everything below/above empty, can safely expand
        if (cands.size() == 0)
            return targety;
        //try to align the candidate boxes
        for (Iterator<AreaImpl> it = cands.iterator(); it.hasNext(); )
        {
            AreaImpl cand = it.next();
            if (sep && 
                    ((down && separatorUp(cand.getGridPosition())) ||
                     (!down && separatorDown(cand.getGridPosition()))))
                return na; //separated, cannot expand
            else if ((matchstyles && !cand.hasSameStyle(template)) || cand.getLevel() > maxlevel)
                return na; //not the same style or level
            else
            {
                Rectangular cgp = new Rectangular(cand.getGridPosition());
                if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                    return targety; //simple match
                else if (cgp.getX1() < gp.getX1() || cgp.getX2() > gp.getX2())
                    return na; //area overflows, cannot expand
                else //candidate is smaller, try to expand align to our width
                {
                    if (down)
                    {
                        Rectangular newlimit = new Rectangular(gp.getX1(), targety, gp.getX2(), limit.getY2());
                        expandToLimit(cand, cgp, newlimit, template, true, false, DIR_RIGHT, REQ_HORIZONTAL);
                        if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                            return cgp.getY2(); //successfully aligned
                    }
                    else
                    {
                        Rectangular newlimit = new Rectangular(gp.getX1(), limit.getY1(), gp.getX2(), targety);
                        expandToLimit(cand, cgp, newlimit, template, true, false, DIR_RIGHT, REQ_HORIZONTAL);
                        if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                            return cgp.getY1(); //successfully aligned
                    }
                }
            }
        }
        return na; //some candidates but none usable
    }
    
    /**
     * Try to expand the area horizontally by a smallest step possible
     * @param gp the area position in the grid
     * @param limit the maximal expansion limit
     * @param right <code>true</code> meand expand right, <code>false<code> means expand left
     * @param sep stop on separators
     * @return the new vertical end of the area.
     */ 
    private int expandHorizontally(Rectangular gp, Rectangular limit, AreaImpl template, boolean right, boolean sep)
    {
        //System.out.println("exp: " + gp + (right?" ->":" <-") + " " + sep);
        int na = right ? gp.getX2() : gp.getX1(); //what to return when it's not possible to expand
        int targetx = right ? (gp.getX2() + 1) : (gp.getX1() - 1); 
        //find candidate boxes
        boolean found = false;
        int y = gp.getY1();
        while (y <= gp.getY2()) //scan everything at the target position
        {
            AreaImpl cand = (AreaImpl) getTopology().findAreaAt(targetx, y);
            //ignore candidates that intersect with our area (could leat to an infinite loop)
            if (cand != null && !cand.getGridPosition().intersects(gp))
            {
                found = true;
                if (sep &&
                        ((right && separatorLeft(cand.getGridPosition())) ||
                         (!right && separatorRight(cand.getGridPosition()))))
                    return na; //separated, cannot expand
                else if ((matchstyles && !cand.hasSameStyle(cand)) || cand.getLevel() > maxlevel)
                    return na; //not the same style or level
                else
                {
                    Rectangular cgp = new Rectangular(cand.getGridPosition());
                    if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                        return targetx; //simple match
                    else if (cgp.getY1() < gp.getY1() || cgp.getY2() > gp.getY2())
                        return na; //area overflows, cannot expand
                    else //candidate is smaller, try to expand align to our width
                    {
                        if (right)
                        {
                            Rectangular newlimit = new Rectangular(targetx, gp.getY1(), limit.getX2(), gp.getY2());
                            expandToLimit(cand, cgp, newlimit, template, false, true, DIR_DOWN, REQ_VERTICAL);
                            if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                                return cgp.getX2(); //successfully aligned
                        }
                        else
                        {
                            Rectangular newlimit = new Rectangular(limit.getX1(), gp.getY1(), targetx, gp.getY2());
                            expandToLimit(cand, cgp, newlimit, template, false, true, DIR_DOWN, REQ_VERTICAL);
                            if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                                return cgp.getX1(); //successfully aligned
                        }
                    }
                }
                //skip the candidate
                y += cand.getGridPosition().getY2() + 1;
            }
            else
                y++;
        }
        if (!found)
            return targetx; //everything below/above empty, can safely expand
        else
            return na; //some candidates but none usable
    }
    
    //====================================================================================

    private boolean separatorDown(Rectangular pos)
    {
        if (pos.getY2() < getTopology().getTopologyHeight()-1)
        {
            Rectangular spos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1(), pos.getY2(), pos.getX2(), pos.getY2()));
            Rectangular epos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1(), pos.getY2() + 1, pos.getX2(), pos.getY2() + 1));
            return seps.isSeparatorAt(spos.midX(), spos.getY2()) ||
                   seps.isSeparatorAt(epos.midX(), epos.getY1());
        }
        else
            return true;
    }
    
    private boolean separatorUp(Rectangular pos)
    {
        if (pos.getY1() > 0)
        {
            Rectangular spos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1(), pos.getY1(), pos.getX2(), pos.getY1()));
            Rectangular epos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1(), pos.getY1() - 1, pos.getX2(), pos.getY1() - 1));
            return seps.isSeparatorAt(spos.midX(), spos.getY1()) ||
                   seps.isSeparatorAt(epos.midX(), epos.getY2());
        }
        else
            return true;
    }
    
    private boolean separatorLeft(Rectangular pos)
    {
        if (pos.getX1() > 0)
        {
            Rectangular spos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1(), pos.getY1(), pos.getX1(), pos.getY2()));
            Rectangular epos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX1() - 1, pos.getY1(), pos.getX1() - 1, pos.getY2()));
            return seps.isSeparatorAt(spos.getX1(), spos.midY()) ||
                   seps.isSeparatorAt(epos.getX2(), epos.midY());
        }
        else
            return true;
    }
    
    private boolean separatorRight(Rectangular pos)
    {
        if (pos.getX2() < getTopology().getTopologyWidth()-1)
        {
            Rectangular spos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX2(), pos.getY1(), pos.getX2(), pos.getY2()));
            Rectangular epos = getTopology().toPixelPositionAbsolute(new Rectangular(pos.getX2() + 1, pos.getY1(), pos.getX2() + 1, pos.getY2()));
            return seps.isSeparatorAt(spos.getX2(), spos.midY()) ||
                   seps.isSeparatorAt(epos.getX1(), epos.midY());
        }
        else
            return true;
    }
}
