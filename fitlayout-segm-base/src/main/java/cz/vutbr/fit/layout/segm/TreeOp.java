/**
 * TreeOp.java
 *
 * Created on 23. 10. 2020, 9:19:01 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;

/**
 * Implementation of basic area and tree opeations used by the operators.
 * 
 * @author burgetr
 */
public class TreeOp
{
    private static Logger log = LoggerFactory.getLogger(TreeOp.class);
    
    /**
     * Joins the target area with another area and updates the layout in the grid to the given values.
     * Moves the children of the otherarea to the target area.
     * @param target The target area
     * @param other The area to be joined to the target area
     * @param pos The position of the result in the grid
     * @param horizontal Horizontal or vertical join?
     * @param flatten Remove the child areas, preserve the boxes only.
     */
    public static void joinArea(Area target, Area other, Rectangular pos, boolean horizontal, boolean flatten)
    {
        target.setGridPosition(pos);
        if (flatten)
        {
            // use area text for names of non-leaf areas
            final String name1 = target.isLeaf() ? target.getName() : target.getText();
            final String name2 = other.isLeaf() ? other.getName() : other.getText();
            // flattening - remove all the child areas
            target.removeAllChildren();
            target.setName(name1 + " . " + name2);
        }
        else
        {
            // not flattening - move the child areas of the other area to the target area
            if (other.getChildCount() > 0)
            {
                final List<Area> adopt = new ArrayList<>(other.getChildren());
                for (Iterator<Area> it = adopt.iterator(); it.hasNext();)
                    target.appendChild(it.next());
            }
            target.setName(target.getName() + " . " + other.getName());
        }
        join(target, other, horizontal);
        //copy the tag while preserving the higher support //TODO is this corect?
        for (Map.Entry<Tag, Float> entry : other.getTags().entrySet())
        {
            if (!target.getTags().containsKey(entry.getKey()) || entry.getValue() > target.getTags().get(entry.getKey()))
                target.getTags().put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Joins another area to the target area. Updates the bounds and the name accordingly.
     * @param target The target area.
     * @param other The area to be joined to target area.
     * @param horizontal If true, the areas are joined horizontally.
     * This influences the resulting area borders. If false, the areas are joined vertically.
     */
    public static void join(Area target, Area other, boolean horizontal)
    {
        target.getBounds().expandToEnclose(other.getBounds());
        //update border information according to the mutual area positions
        if (horizontal)
        {
            if (target.getX1() <= other.getX1())
            {
                if (other.hasRightBorder())
                    target.setBorderStyle(Side.RIGHT, other.getBorderStyle(Side.RIGHT));
            }
            else
            {
                if (other.hasLeftBorder())
                    target.setBorderStyle(Side.LEFT, other.getBorderStyle(Side.LEFT));
            }
        }
        else
        {
            if (target.getY1() <= other.getY1())
            {
                if (other.hasBottomBorder())
                    target.setBorderStyle(Side.BOTTOM, other.getBorderStyle(Side.BOTTOM));
            }
            else
            {
                if (other.hasTopBorder())
                    target.setBorderStyle(Side.TOP, other.getBorderStyle(Side.TOP));
            }
        }
        //add all the contained boxes
        target.getBoxes().addAll(other.getBoxes());
        target.getTextStyle().updateAverages(other.getTextStyle());
        //just a test
        if (!AreaStyle.hasEqualBackground(target, other))
            log.error("Area: Warning: joining areas {} and {} of different background colors {} x {}",
                    target.getName(), other.getName(), target.getBackgroundColor(), other.getBackgroundColor()); 
    }

    /**
     * Joins a child area to this area. Updates the bounds and the name accordingly.
     * @param other The child area to be joined to this area.
     */
    public static void joinChild(Area target, Area other)
    {
        for (Box box : other.getBoxes())
            target.addBox(box);
        target.getBounds().expandToEnclose(other.getBounds());
        target.setName(target.getName() + " . " + other.getName());
    }
    
}
