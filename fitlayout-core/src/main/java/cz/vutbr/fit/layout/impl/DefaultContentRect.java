/**
 * DefaultContentRect.java
 *
 * Created on 21. 11. 2014, 11:25:00 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.GenericTreeNode;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Border.Style;

/**
 * A default ContentRect implementation. This class is usually not used
 * directly; the {@link DefaultBox} and {@link DefaultArea} subclasses
 * should be used instead. 
 * 
 * @author burgetr
 */
public class DefaultContentRect<T extends GenericTreeNode<T>> extends DefaultTreeNode<T> implements ContentRect
{
    private static int nextid = 1;
    
    private int id;
    private IRI pageIri;
    private Rectangular bounds;
    private Color backgroundColor;
    private float underline;
    private float lineThrough;
    private float fontSize;
    private float fontStyle;
    private float fontWeight;
    private boolean backgroundSeparated;
    
    private Border topBorder;
    private Border bottomBorder;
    private Border leftBorder;
    private Border rightBorder;

    
    public DefaultContentRect(Class<T> myType)
    {
        super(myType);
        id = nextid++;
        bounds = new Rectangular();
        topBorder = new Border();
        bottomBorder = new Border();
        leftBorder = new Border();
        rightBorder = new Border();
    }
    
    public DefaultContentRect(Class<T> myType, DefaultContentRect<T> src)
    {
        super(myType);
        id = nextid++;
        pageIri = src.pageIri;
        bounds = new Rectangular(src.bounds);
        backgroundColor = (src.backgroundColor == null) ? null : new Color(src.backgroundColor.getRed(), src.backgroundColor.getGreen(), src.backgroundColor.getBlue());
        underline = src.underline;
        lineThrough = src.lineThrough;
        fontSize = src.fontSize;
        fontWeight= src.fontWeight;
        topBorder = src.topBorder;
        bottomBorder = src.bottomBorder;
        leftBorder = src.leftBorder;
        rightBorder = src.rightBorder;
        backgroundSeparated = src.backgroundSeparated;
    }
    
    @Override
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    @Override
    public IRI getPageIri()
    {
        return pageIri;
    }
    
    public void setPageIri(IRI pageIri)
    {
        this.pageIri = pageIri;
    }
    
    @Override
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }
    
    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }
    
    @Override
    public boolean isBackgroundSeparated()
    {
        return backgroundSeparated;
    }
    
    public void setBackgroundSeparated(boolean backgroundSeparated)
    {
        this.backgroundSeparated = backgroundSeparated;
    }
    
    @Override
    public float getUnderline()
    {
        return underline;
    }
    
    public void setUnderline(float underline)
    {
        this.underline = underline;
    }
    
    @Override
    public float getLineThrough()
    {
        return lineThrough;
    }
    
    public void setLineThrough(float lineThrough)
    {
        this.lineThrough = lineThrough;
    }
    
    @Override
    public float getFontSize()
    {
        return fontSize;
    }
    
    public void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }
    
    @Override
    public float getFontStyle()
    {
        return fontStyle;
    }
    
    public void setFontStyle(float fontStyle)
    {
        this.fontStyle = fontStyle;
    }
    
    @Override
    public float getFontWeight()
    {
        return fontWeight;
    }
    
    public void setFontWeight(float fontWeight)
    {
        this.fontWeight = fontWeight;
    }
    
    @Override
    public int getTopBorder()
    {
        return topBorder.getWidth();
    }

    public void setTopBorder(int width)
    {
        topBorder.setWidth(width);
        if (topBorder.getStyle() == Style.NONE)
            topBorder.setStyle(Style.SOLID);
    }

    @Override
    public int getBottomBorder()
    {
        return bottomBorder.getWidth();
    }

    public void setBottomBorder(int width)
    {
        bottomBorder.setWidth(width);
        if (bottomBorder.getStyle() == Style.NONE)
            bottomBorder.setStyle(Style.SOLID);
    }

    @Override
    public int getLeftBorder()
    {
        return leftBorder.getWidth();
    }

    public void setLeftBorder(int width)
    {
        leftBorder.setWidth(width);
        if (leftBorder.getStyle() == Style.NONE)
            leftBorder.setStyle(Style.SOLID);
    }

    @Override
    public int getRightBorder()
    {
        return rightBorder.getWidth();
    }

    public void setRightBorder(int width)
    {
        rightBorder.setWidth(width);
        if (rightBorder.getStyle() == Style.NONE)
            rightBorder.setStyle(Style.SOLID);
    }

    @Override
    public boolean hasTopBorder()
    {
        return topBorder.getStyle() != Style.NONE;
    }

    @Override
    public boolean hasBottomBorder()
    {
        return bottomBorder.getStyle() != Style.NONE;
    }

    @Override
    public boolean hasLeftBorder()
    {
        return leftBorder.getStyle() != Style.NONE;
    }

    @Override
    public boolean hasRightBorder()
    {
        return rightBorder.getStyle() != Style.NONE;
    }

    /**
     * Sets all the border values.
     * @param top
     * @param right
     * @param bottom
     * @param left
     */
    public void setBorders(int top, int right, int bottom, int left)
    {
        setTopBorder(top);
        setRightBorder(right);
        setBottomBorder(bottom);
        setLeftBorder(left);
    }
    
    @Override
    public Border getBorderStyle(Side side)
    {
        switch (side)
        {
            case TOP:
                return topBorder;
            case LEFT:
                return leftBorder;
            case BOTTOM:
                return bottomBorder;
            case RIGHT:
                return rightBorder;
        }
        return null;
    }
    
    public void setBorderStyle(Side side, Border style)
    {
        switch (side)
        {
            case TOP:
                topBorder = new Border(style);
                break;
            case LEFT:
                leftBorder = new Border(style);
                break;
            case BOTTOM:
                bottomBorder = new Border(style);
                break;
            case RIGHT:
                rightBorder = new Border(style);
                break;
        }
    }
    
    @Override
    public Rectangular getBounds()
    {
        return bounds;
    }
    
    public void setBounds(Rectangular bounds)
    {
        this.bounds = bounds;
    }
    
    @Override
    public int getBorderCount()
    {
        int bcnt = 0;
        if (hasTopBorder()) bcnt++;
        if (hasBottomBorder()) bcnt++;
        if (hasLeftBorder()) bcnt++;
        if (hasRightBorder()) bcnt++;
        return bcnt;
    }

    @Override
    public int getX1()
    {
        return getBounds().getX1();
    }

    @Override
    public int getY1()
    {
        return getBounds().getY1();
    }

    @Override
    public int getX2()
    {
        return getBounds().getX2();
    }

    @Override
    public int getY2()
    {
        return getBounds().getY2();
    }

    @Override
    public int getWidth()
    {
        return getBounds().getWidth();
    }

    @Override
    public int getHeight()
    {
        return getBounds().getHeight();
    }

    @Override
    public void move(int xofs, int yofs)
    {
        getBounds().move(xofs, yofs);
        for (T child : getChildren())
        {
            if (child instanceof ContentRect) 
                ((ContentRect) child).move(xofs, yofs);
        }
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked")
        DefaultContentRect<T> other = (DefaultContentRect<T>) obj;
        if (id != other.id) return false;
        return true;
    }

    public static void resetId()
    {
        nextid = 1;
    }
    
}
