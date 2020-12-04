package cz.vutbr.fit.layout.bcs.impl;

import java.awt.Color;
import java.util.ArrayList;

public class SimplePattern
{
    private final ArrayList<PageArea> children;

    /* DOC: alignment is the basic feature of pattern - if boxes are not aligned, they are not in pattern */
    private int alignment;

    public static final int ALIGNMENT_NONE = 0;
    public static final int ALIGNMENT_TOP = 1;
    public static final int ALIGNMENT_BOTTOM = 2;
    public static final int ALIGNMENT_LEFT = 3;
    public static final int ALIGNMENT_RIGHT = 4;

    private Color meanColor;
    private double colorTolerance; /* This is basically standard deviation from the mean color */

    /* DOC: this is depending on alignment
     * - if alignment is vertical, this will be height similarity
     * - if alignment is horizontal, this will be width similarity
     */
    private double meanSize;
    private double sizeTolerance;

    public SimplePattern(ArrayList<PageArea> c)
    {
        this.children = c;

        this.evaluate();
    }

    public void evaluate()
    {
        int i;
        PageArea a, b;
        double sum, sizeSum;
        int rSum, gSum, bSum;
        double tmpVal;
        int alignment;

        if (this.children.size() == 0) this.alignment = ALIGNMENT_NONE;

        /* First figure out alignment */
        a = this.children.get(0);
        for (i = 1; i < this.children.size(); i++)
        {
            b = this.children.get(i);
            alignment = this.getAlignment(a, b);
            if (alignment == ALIGNMENT_NONE)
            {
                return;
            }
            else
            {
                if (this.alignment == ALIGNMENT_NONE)
                {
                    this.alignment = alignment;
                }
                else if (this.alignment != alignment)
                {
                    this.alignment = ALIGNMENT_NONE;
                    return;
                }
            }
        }

        /* Color and size similarity is all that remains */
        /* DOC: size is always measured on the opposite
         * direction to the alignment (horizontal vs. vertical) */
        sizeSum = rSum = gSum = bSum = 0;
        for (PageArea child: this.children)
        {
            rSum += child.getColor().getRed();
            gSum += child.getColor().getGreen();
            bSum += child.getColor().getBlue();
            if (this.alignment == ALIGNMENT_TOP || this.alignment == ALIGNMENT_BOTTOM)
            {
                sizeSum += child.getHeight();
            }
            else
            {
                sizeSum += child.getWidth();
            }
        }
        rSum /= this.children.size();
        gSum /= this.children.size();
        bSum /= this.children.size();
        this.meanColor = new Color(rSum, gSum, bSum);
        this.meanSize = sizeSum/this.children.size();

        sum = sizeSum = 0;
        for (PageArea child: this.children)
        {
            tmpVal = PageArea.colorDiff(child.getColor(), this.meanColor);
            sum += tmpVal*tmpVal;
            if (this.alignment == ALIGNMENT_TOP || this.alignment == ALIGNMENT_BOTTOM)
            {
                tmpVal = this.meanSize-child.getHeight();
            }
            else
            {
                tmpVal = this.meanSize-child.getWidth();
            }
            sizeSum += tmpVal*tmpVal;
        }
        sum /= this.children.size();
        sizeSum /= this.children.size();

        this.colorTolerance = Math.sqrt(sum);
        if (this.colorTolerance == 0) this.colorTolerance = 5; /* Default value is 5% TODO: is this correct */
        this.sizeTolerance = Math.sqrt(sizeSum);
    }

    public boolean match(PageArea a)
    {
        PageArea child;
        double sizeDiff;
        double colorDiff;

        if (this.children.size() == 0) return true;

        /* There is no pattern in the group, there can be none after this is added */
        if (this.alignment == ALIGNMENT_NONE) return false;

        /* First the alignment check */
        child = this.children.get(0);
        if ((this.alignment == ALIGNMENT_TOP && child.getTop() != a.getTop()) ||
            (this.alignment == ALIGNMENT_BOTTOM && child.getBottom() != a.getBottom()) ||
            (this.alignment == ALIGNMENT_LEFT && child.getLeft() != a.getLeft()) ||
            (this.alignment == ALIGNMENT_RIGHT && child.getRight() != a.getRight()))
        {
            return false;
        }

        /* Now the check for color and size similarity */
        colorDiff = PageArea.colorDiff(this.meanColor, a.getColor());
        if (colorDiff > this.colorTolerance) return false;

        sizeDiff = this.getSizeDiff(a);
        if (sizeDiff > this.sizeTolerance) return false;

        return true;
    }

    private double getSizeDiff(PageArea a)
    {
        if (this.alignment == ALIGNMENT_TOP || this.alignment == ALIGNMENT_BOTTOM)
        {
            return Math.abs(this.meanSize-a.getHeight());
        }
        else
        {
            return Math.abs(this.meanSize-a.getWidth());
        }
    }

    private int getAlignment(PageArea a, PageArea b)
    {
        if (a.getLeft() == b.getLeft()) return ALIGNMENT_LEFT;
        else if (a.getTop() == b.getTop()) return ALIGNMENT_TOP;
        else if (a.getRight() == b.getRight()) return ALIGNMENT_RIGHT;
        else if (a.getBottom() == b.getBottom()) return ALIGNMENT_BOTTOM;

        else return ALIGNMENT_NONE;
    }

    public double getWidthRatio(PageArea a, PageArea b)
    {
        /* DOC: this is the same formula base as PageArea::getSizeSimilarity() uses  */
        return (double)Math.abs(a.getWidth()-b.getWidth())/(a.getWidth()+ b.getWidth());
    }

    public double getHeightRatio(PageArea a, PageArea b)
    {
        /* DOC: this is the same formula base as PageArea::getSizeSimilarity() uses  */
        return (double)Math.abs(a.getHeight()-b.getHeight())/(a.getHeight()+ b.getHeight());
    }
}
