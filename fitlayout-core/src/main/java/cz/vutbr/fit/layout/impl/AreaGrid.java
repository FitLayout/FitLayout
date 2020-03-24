/**
 * AreaGrid.java
 *
 * Created on 29.6.2006, 10:30:36 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Arrays;
import java.util.List;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A grid of areas in an abstract parent rectangle.
 * The grid is used as the default area topology.
 * 
 * @author burgetr
 */
public class AreaGrid
{
    /** The maximal difference between two lengths that are considered a "being the same" */
    public static final int GRID_THRESHOLD = 0;
    
    /** Number of columns */
    private int width;
    
    /** Minimal indentation level */
    private int minindent;
    
    /** Maximal indentation level */
    private int maxindent;

    /** Array of column widths */
    private int[] cols;
    
    /** Number of rows */
    private int height;
    
    /** Array of row heights */
    private int[] rows;
    
    /** Absolute coordinates of the parent area */
    private Rectangular abspos;
    
    /** The list of areas laid out in this grid */
    private List<Area> areas;
    
    /** The target topology where the computed positions will be set */
    private AreaTopology target;
    
    //================================================================================
    
    /**
     * Constructs a grid of all the child areas of the given parent area.
     * @param area the parent area whose children will be laid out in the grid
     * @param targetTopology the area topology where the computed grid positions will be set
     */
    public AreaGrid(DefaultArea area, AreaTopology targetTopology)
    {
        abspos = area.getBounds();
        areas = area.getChildren();
        target = targetTopology;
        calculateColumns();
        calculateRows();
    }
    
    /**
     * Constructs a grid from the list of areas.
     * @param position Absolute position of the grid area
     * @param areas the areas to be laid out in the grid.
     * @param targetTopology the area topology where the computed grid positions will be set
     */
    public AreaGrid(Rectangular position, List<Area> areas, AreaTopology targetTopology)
    {
        this.abspos = position;
        this.areas = areas;
        target = targetTopology;
        calculateColumns();
        calculateRows();
    }
    
    //================================================================================
    
    /**
     * Obtains the absolute position where the grid is placed within the page.
     * @return
     */
    public Rectangular getAbsolutePosition()
    {
        return abspos;
    }
    
    /**
     * @return Returns the cols.
     */
    public int[] getCols()
    {
        return cols;
    }

    /**
     * @return Returns the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @return Returns the rows.
     */
    public int[] getRows()
    {
        return rows;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }
    
    public int getMinIndent()
    {
        return minindent;
    }
    
    public int getMaxIndent()
    {
        return maxindent;
    }
    
    public String toString()
    {
    	return "Grid " + width + "x" + height;
    }
    
    /**
     * Finds the offset of the specified column from the grid origin.
     * @param col the column index
     * @return the offset in pixels. Column 0 has always the offset 0.
     */
    public int getColOfs(int col) throws ArrayIndexOutOfBoundsException
    {
    	if (col < width)
    	{
    		int ofs = 0;
    		for (int i = 0; i < col; i++)
    			ofs += cols[i];
    		return ofs;
    	}
    	else if (col == width)
    		return abspos.getWidth();
    	else
    		throw new ArrayIndexOutOfBoundsException(col + ">" + width);
    }
    
    /**
     * Finds the offset of the specified row from the grid origin.
     * @param col the row index
     * @return the offset in pixels. Row 0 has always the offset 0.
     */
    public int getRowOfs(int row) throws ArrayIndexOutOfBoundsException
    {
    	if (row < height)
    	{
	    	int ofs = 0;
	    	for (int i = 0; i < row; i++)
	    		ofs += rows[i];
	    	return ofs;
    	}
    	else if (row == height)
    		return abspos.getHeight();
    	else
    		throw new ArrayIndexOutOfBoundsException(row + ">" + height);
    }
    
    /**
     * Computes the coordinates of the specified grid cell relatively to the area top left corner.
     * @param x the column index of the cell
     * @param y the row index of the cell
     * @return the coordinates of the given cell in pixels
     */
    public Rectangular getCellBoundsRelative(int x, int y)
    {
        int x1 = getColOfs(x);
        int y1 = getRowOfs(y);
        int x2 = (x == width-1) ? abspos.getWidth() - 1 : x1 + cols[x] - 1;
        int y2 = (y == height-1) ? abspos.getHeight() - 1 : y1 + rows[y] - 1;
        return new Rectangular(x1, y1, x2, y2);
    }
    
    /**
     * Computes the absolute coordinates of the specified grid cell.
     * @param x the column index of the cell
     * @param y the row index of the cell
     * @return the coordinates of the given cell in pixels
     */
    public Rectangular getCellBoundsAbsolute(int x, int y)
    {
        int x1 = abspos.getX1() + getColOfs(x);
        int y1 = abspos.getY1() + getRowOfs(y);
        int x2 = ((x == width-1) ? abspos.getX1() + abspos.getWidth() - 1 : x1 + cols[x] - 1);
        int y2 = ((y == height-1) ? abspos.getY1()+ abspos.getHeight() - 1 : y1 + rows[y] - 1);
        return new Rectangular(x1, y1, x2, y2);
    }
    
    /**
     * Computes the absolute coordinates of the specified area in the grid.
     * @param x1 the column index of the top left cell of the area.
     * @param y1 the row index of the top left cell of the area.
     * @param x2 the column index of the bottom right cell of the area.
     * @param y2 the row index of the bottom right cell of the area.
     * @return the absolute coordinates of the given area in pixels.
     */
    public Rectangular getAreaBoundsAbsolute(int x1, int y1, int x2, int y2)
    {
        final Rectangular end = getCellBoundsAbsolute(x2, y2);
        return new Rectangular(abspos.getX1() + getColOfs(x1), abspos.getY1() + getRowOfs(y1),
                end.getX2(), end.getY2());
    }
    
    /**
     * Computes the absolute coordinates of the specified area in the grid.
     * @param The area coordinates in the grid.
     * @return the absolute coordinates of the given area in pixels.
     */
    public Rectangular getAreaBoundsAbsolute(Rectangular area)
    {
        return getAreaBoundsAbsolute(area.getX1(), area.getY1(), area.getX2(), area.getY2());
    }
    
    /**
     * Finds a grid cell that contains the specified point
     * @param x the x coordinate of the specified point
     * @return the X offset of the grid cell that contains the specified absolute 
     * x coordinate or -1 when there is no such cell
     */
    public int findCellX(int x)
    {
    	int ofs = abspos.getX1();
    	for (int i = 0; i < cols.length; i++)
    	{
    		ofs += cols[i];
    		if (x < ofs)
    			return i;
    	}
    	return -1;
    }
    
    /**
     * Finds a grid cell that contains the specified point
     * @param y the y coordinate of the specified point
     * @return the Y offset of the grid cell that contains the specified absolute 
     * y coordinate or -1 when there is no such cell
     */
    public int findCellY(int y)
    {
    	int ofs = 0;
    	for (int i = 0; i < rows.length; i++)
    	{
    		ofs += rows[i];
    		if (y < ofs + abspos.getY1())
    			return i;
    	}
    	return -1;
    }
    
    //================================================================================
    
    /**
     * @return <code>true</code> if the values are equal in the specified threshold
     */
    private boolean theSame(int val1, int val2)
    {
        return Math.abs(val2 - val1) <= GRID_THRESHOLD;
    }
    
    /**
     * Goes through the child areas and creates a list of collumns
     */
	private void calculateColumns()
    {
        //create the sorted list of points
        GridPoint points[] = new GridPoint[areas.size() * 2];
        int pi = 0;
        for (Area area : areas)
        {
            points[pi] = new GridPoint(area.getX1(), area, true);
            points[pi+1] = new GridPoint(area.getX2() + 1, area, false);
            pi += 2;
            //X2+1 ensures that the end of one box will be on the same point
            //as the start of the following box
        }
        Arrays.sort(points);
        
        //calculate the number of columns
        int cnt = 0;
        int last = abspos.getX1();
        for (int i = 0; i < points.length; i++)
            if (!theSame(points[i].value, last))
            { 
                last = points[i].value;
                cnt++;
            }
        if (!theSame(last, abspos.getX2()))
        	cnt++; //last column finishes the whole area
        width = cnt;
        
        //calculate the column widths and the layout
        maxindent = 0;
        minindent = -1;
        cols = new int[width];
        cnt = 0;
        last = abspos.getX1();
        for (int i = 0; i < points.length; i++)
        {
            if (!theSame(points[i].value, last)) 
            {
                cols[cnt] = points[i].value - last;
                last = points[i].value;
                cnt++;
            }
            if (points[i].begin)
            {
                target.getPosition(points[i].area).setX1(cnt);
                maxindent = cnt;
                if (minindent == -1) minindent = maxindent;
                //points[i].node.getArea().setX1(parent.getArea().getX1() + getColOfs(cnt));
            }
            else
            {
                Rectangular pos = target.getPosition(points[i].area); 
                pos.setX2(cnt-1);
                if (pos.getX2() < pos.getX1())
                    pos.setX2(pos.getX1());
                //points[i].node.getArea().setX2(parent.getArea().getX1() + getColOfs(pos.getX2()+1));
            }
        }
        if (!theSame(last, abspos.getX2()))
        	cols[cnt] = abspos.getX2() - last;
        if (minindent == -1)
            minindent = 0;
    }

    /**
     * Goes through the child areas and creates a list of rows
     */
	private void calculateRows()
    {
        //create the sorted list of points
        GridPoint points[] = new GridPoint[areas.size() * 2];
        int pi = 0;
        for (Area area : areas)
        {
            points[pi] = new GridPoint(area.getY1(), area, true);
            points[pi+1] = new GridPoint(area.getY2() + 1, area, false);
            pi += 2;
            //Y2+1 ensures that the end of one box will be on the same point
            //as the start of the following box
        }
        Arrays.sort(points);
        
        //calculate the number of rows
        int cnt = 0;
        int last = abspos.getY1();
        for (int i = 0; i < points.length; i++)
            if (!theSame(points[i].value, last))
            { 
                last = points[i].value;
                cnt++;
            }
        if (!theSame(last, abspos.getY2()))
        	cnt++; //last row finishes the whole area
        height = cnt;
        
        //calculate the row heights and the layout
        rows = new int[height];
        cnt = 0;
        last = abspos.getY1();
        for (int i = 0; i < points.length; i++)
        {
            if (!theSame(points[i].value, last)) 
            {
                rows[cnt] = points[i].value - last;
                last = points[i].value;
                cnt++;
            }
            if (points[i].begin)
            {
                target.getPosition(points[i].area).setY1(cnt);
                //points[i].node.getArea().setY1(parent.getArea().getY1() + getRowOfs(cnt));
            }
            else
            {
                Rectangular pos = target.getPosition(points[i].area); 
                pos.setY2(cnt-1);
                if (pos.getY2() < pos.getY1())
                    pos.setY2(pos.getY1());
                //points[i].node.getArea().setY2(parent.getArea().getY1() + getRowOfs(pos.getY2()+1));
            }
        }
        if (!theSame(last, abspos.getY2()))
        	rows[cnt] = abspos.getY2() - last;
    }
    
    
}

/** A point in the grid */
class GridPoint implements Comparable<GridPoint>
{
    public int value;       //the point position
    public Area area;       //the corresponding visual area
    public boolean begin;   //is it the begining or the end of the node?
    
    public GridPoint(int value, Area area, boolean begin)
    {
        this.value = value;
        this.area = area;
        this.begin = begin;
    }
    
    public int compareTo(GridPoint other)
    {
        return value - other.value;
    }
}
