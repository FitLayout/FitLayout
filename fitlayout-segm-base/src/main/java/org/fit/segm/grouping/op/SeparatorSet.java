/**
 * 
 */
package org.fit.segm.grouping.op;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.fit.layout.model.Area;
import org.fit.segm.grouping.AreaImpl;

/**
 * A generic set of horizontal and vertical separators for a page.
 * @author radek
 */
public abstract class SeparatorSet
{
    /** The minimal height of a horizontal separator in "em" units */
    protected static final double HSEP_MIN_HEIGHT = 0.1; 
    
    /** The minimal width of a vertical separator in "em" units */
    protected static final double VSEP_MIN_WIDTH = 0.1;
    
    /** The minimal width/height ratio of the separator */
    protected static final double SEP_MIN_RATIO = 1;
    
    /** The width of the 'artificial' separators created by background colors */
    protected static final int ART_SEP_WIDTH = 1;
    
	/** The root of the area tree that will be processed */
	protected AreaImpl root;
	
	/** List of horizontal separators */
	protected Vector<Separator> hsep;
	
	/** List of vertical separators */
	protected Vector<Separator> vsep;

    /** List of separators comming from the box analysis */
    protected Vector<Separator> bsep;
	
	/**
	 * Creates a new separator set with one horizontal and one vertical separator.
	 */
	public SeparatorSet(AreaImpl root)
	{
        this.root = root;
        init(null);
	}

    /**
     * Creates a new separator set with one horizontal and one vertical separator.
     */
    public SeparatorSet(AreaImpl root, Area filter)
    {
        this.root = root;
        init(filter);
    }
    
    private void init(Area filter)
    {
        findAreaSeparators(root);
        findSeparators(root, filter);
    }
    
    //=====================================================================================
    
    public Vector<Separator> getHorizontal()
    {
        return hsep;
    }
    
    public Vector<Separator> getVertical()
    {
        return vsep;
    }
    
    public Vector<Separator> getBoxsep()
    {
        return bsep;
    }
    
    /**
     * Obtains the most important (with the greatest weight) separator from all the separators.
     * The separators must be sorted before this metod is called.
     * @return The selected separator or <code>null</code> when there are no separators
     */
    public Separator getMostImportantSeparator()
    {
        Separator sep = null;
        if (!hsep.isEmpty())
            sep = hsep.firstElement();
        if (!vsep.isEmpty() && (sep == null || vsep.firstElement().getWeight() >= sep.getWeight()))
            sep = vsep.firstElement();
        if (!bsep.isEmpty() && (sep == null || bsep.firstElement().getWeight() >= sep.getWeight()))
            sep = bsep.firstElement();
        return sep;
    }
    
    //=====================================================================================
    
    /**
     * Computes the minimal height of a horizontal separator that is accepted with this separator set.
     * Usually, it depends on the average font size of the corresponding visual area.
     * @return the minimal height in pixels
     */
    public int getMinHSepHeight()
    {
        return (int) (root.getFontSize() * HSEP_MIN_HEIGHT);
    }
    
    /**
     * Computes the minimal width of a vertical separator that is accepted with this separator set.
     * Usually, it depends on the average font size of the corresponding visual area.
     * @return the minimal width in pixels
     */
    public int getMinVSepWidth()
    {
        return (int) (root.getFontSize() * VSEP_MIN_WIDTH);
    }
    
    //=====================================================================================
    
    /**
     * Checks if a point is covered by a separator.
     * @param x the point x coordinate
     * @param y the point y coordinate
     * @return <code>true</code> if any of the separators in this set covers the specified point
     */
    public boolean isSeparatorAt(int x, int y)
    {
        return containsSeparatorAt(x, y, bsep) ||
               containsSeparatorAt(x, y, hsep) ||
               containsSeparatorAt(x, y, vsep);
    }
    
    private boolean containsSeparatorAt(int x, int y, Vector<Separator> col)
    {
        for (Iterator<Separator> it = col.iterator(); it.hasNext();)
        {
        	Separator sep = it.next();
            if (sep.contains(x, y))
                return true;
        }
        return false;
    }
    
    //=====================================================================================
    
    
    /**
     * Finds the horizontal and vertical list of separators
     * @param area the root area
     * @param filter if not null, only the sub areas enclosed in the filter area
     * 	are considered
     */
    protected abstract void findSeparators(AreaImpl area, Area filter);
    
    /**
     * Applies various filters on the current separator sets in order to remove irrelevant separators or adjust the sizes.
     * This is calle automatically after each recursive iteration.
     */
    protected void applyRegularFilters()
    {
        //this is the default implementation - we filter the separators by their widths and we process the intersections somehow
        filterSeparators();
        processIntersections();
    }
    
    /**
     * Applies various filters on the current separator sets in order to remove irrelevant separators or adjust the sizes.
     * This must be called manually after the final results are obtained.
     */
    public void applyFinalFilters()
    {
        //this is the default implementation - we filter the separators by their widths and we process the intersections somehow
        filterMarginalSeparators();
        filterSeparators();
        processIntersections();
        sortSeparators();
    }
    
    //=====================================================================================

    /**
     * Removes the separators that are placed on the area borders.
     */
    protected void filterMarginalSeparators()
    {
        for (Iterator<Separator> it = hsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            if (sep.getY1() == root.getY1() || sep.getY2() == root.getY2())
                it.remove();
        }
        for (Iterator<Separator> it = vsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            if (sep.getX1() == root.getX1() || sep.getX2() == root.getX2())
                it.remove();
        }
    }
    
    /**
     * Removes all the separators where the weight is lower than the specified threshold.
     */
    protected void filterSeparators()
    {
        /*int hthreshold = (int) (root.getArea().getDeclaredFontSize() * HSEP_MIN_HEIGHT);
        int vthreshold = (int) (root.getArea().getDeclaredFontSize() * VSEP_MIN_WIDTH);*/
        int hthreshold = getMinHSepHeight();
        int vthreshold = getMinVSepWidth();
        
        for (Iterator<Separator> it = hsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            //Adaptive height threshold: use the font size of the box above the separator for determining the em size for the threshold  
            AreaImpl above = root.findContentAbove(sep);
            if (above != null)
                hthreshold = (int) (above.getFontSize() * HSEP_MIN_HEIGHT);
            else
                hthreshold = (int) (root.getFontSize() * HSEP_MIN_HEIGHT);
            //System.out.println("For: " + sep + " limit " + hthreshold + " area " + above);
            
            if (sep.getWeight() < hthreshold)
                it.remove();
                //System.out.println("removed");
            else if (sep.getWidth() / (double) sep.getHeight() < SEP_MIN_RATIO)
                it.remove();
        }
        for (Iterator<Separator> it = vsep.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            if (sep.getWeight() < vthreshold)
                it.remove();
            else if (sep.getHeight() / (double) sep.getWidth() < SEP_MIN_RATIO)
                it.remove();
        }
    }
    
    /**
     * Processes the separators so that they do not intersect. 
     */
    protected void processIntersections()
    {
        //processIntersectionsRemoveHorizontal();
    }
    
    /**
     * Processes the separators so that they do not intersect.
     * The vertical separators are left untouched, the horizontal separators are
     * split by the vertical ones when necessary.
     */
    protected void processIntersectionsSplitHorizontal()
    {
        boolean change;
        do
        {
            Vector<Separator> newsep = new Vector<Separator>(hsep.size());
            change = false;
            for (Separator hs : hsep)
            {
            	boolean split = false;
                for (Separator vs : vsep)
                {
                    if (hs.intersects(vs))
                    {
                        Separator nhs = hs.hsplit(vs);
                        newsep.add(hs);
                        if (nhs != null)
                            newsep.add(nhs);
                        split = true;
                        change = true;
                        break; //do not try other vertical seps
                    }
                }
                if (!split)
                	newsep.add(hs);
            }
            hsep = newsep;
        } while (change);
    }
    
    /**
     * Processes the separators so that they do not intersect.
     * The vertical separators are left untouched, the horizontal separators are
     * removed when they intersect with a vertical one.
     */
    protected void processIntersectionsRemoveHorizontal()
    {
        for (Iterator<Separator> hit = hsep.iterator(); hit.hasNext(); )
        {
        	Separator hs = hit.next();
            for (Separator vs : vsep)
            {
                if (hs.intersects(vs))
                {
                	hit.remove();
                	break;
                }
            }
        }
    }
    
    private void sortSeparators()
    {
        Collections.sort(hsep);
        Collections.sort(vsep);
        Collections.sort(bsep);
    }
    
    //=====================================================================================

    /**
     * Creates a list of separators that are implemented as visual area borders.
     */
    private void findAreaSeparators(AreaImpl root)
    {
        bsep = new Vector<Separator>();
        for (int i = 0; i < root.getChildCount(); i++)
        {
            Area child = root.getChildAt(i);
            if (child instanceof AreaImpl)
            analyzeAreaSeparators((AreaImpl) child);
        }
    }
    
    /**
     * Analyzes the area and detects the separators that are implemented as borders
     * or background changes.
     */
    private void analyzeAreaSeparators(AreaImpl area)
    {
        boolean isep = area.isExplicitlySeparated() || area.isBackgroundSeparated();
        if (isep || area.separatedUp())
            bsep.add(new Separator(Separator.BOXH,
                                   area.getX1(), area.getY1(), area.getX2(), area.getY1() + ART_SEP_WIDTH - 1));
        if (isep || area.separatedDown())
            bsep.add(new Separator(Separator.BOXH,
                                   area.getX1(), area.getY2() - ART_SEP_WIDTH + 1, area.getX2(), area.getY2()));
        if (isep || area.separatedLeft())
            bsep.add(new Separator(Separator.BOXV,
                                   area.getX1(), area.getY1(), area.getX1() + ART_SEP_WIDTH - 1, area.getY2()));
        if (isep || area.separatedRight())
            bsep.add(new Separator(Separator.BOXV,
                                   area.getX2() - ART_SEP_WIDTH + 1, area.getY1(), area.getX2(), area.getY2()));
    }
    
    //================================================================
    
    //DEBUG FUNCTIONS
    /*protected void dispSeparators()
    {
        BrowserCanvas canv = BlockBrowser.browser.getBrowserCanvas();
        canv.redrawBoxes();
    	for (Separator sep : hsep)
    		dispSep(sep, Color.RED);
    	for (Separator sep : vsep)
    		dispSep(sep, Color.GREEN);
    }
    
    protected void dispSep(Separator a, Color color)
    {
        BrowserCanvas canv = BlockBrowser.browser.getBrowserCanvas();
        java.awt.Graphics g = canv.getImageGraphics();
        //g.setColor(color);
        if (a.isHorizontal())
            g.setColor(Color.RED);
        else 
            //g.setColor(Color.GREEN);
            return;
        g.fillRect(a.getX1(), a.getY1(), a.getWidth(), a.getHeight());
        //g.setColor(Color.BLACK);
        //g.drawRect(a.getX1(), a.getY1(), a.getWidth(), a.getHeight());
        canv.update(canv.getGraphics());
    }
    
    protected void dispRect(Rectangular a, Color color)
    {
        BrowserCanvas canv = BlockBrowser.browser.getBrowserCanvas();
        java.awt.Graphics g = canv.getImageGraphics();
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), 64); //add transparency
        g.setColor(c);
        g.fillRect(a.getX1(), a.getY1(), a.getWidth(), a.getHeight());
        g.setColor(color);
        g.drawRect(a.getX1(), a.getY1(), a.getWidth(), a.getHeight());
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
