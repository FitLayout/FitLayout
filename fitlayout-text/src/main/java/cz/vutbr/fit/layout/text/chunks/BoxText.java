/**
 * BoxText.java
 *
 * Created on 6. 11. 2018, 14:42:49 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.List;

import cz.vutbr.fit.layout.api.AreaUtils;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;


/**
 * A parto of text with connection to source boxes
 * @author burgetr
 */
public class BoxText
{
    private SourceBoxList boxes;
    /** Complete text of the string of boxes */
    private String text;
    /** The individual box string start positions in the complete text */
    private int[] offsets;
    /** The individual box string lengths in the complete text */
    private int[] lengths;
    
    
    public BoxText(SourceBoxList boxes)
    {
        this.boxes = boxes;
        computeOffsets();
    }
    
    public List<Box> getBoxes()
    {
        return boxes;
    }
    
    public int getBoxCount()
    {
        return boxes.size();
    }

    public String getText()
    {
        return text;
    }

    public int[] getOffsets()
    {
        return offsets;
    }

    public int length()
    {
        return text.length();
    }
    
    /**
     * Finds the index of the box that contains the given text position.
     * @param pos The position within the complete text
     * @return The box index for the text positions within the complete text. Otherwise, -1 is returned.
     */
    public int getIndexForPosition(int pos)
    {
        if (pos >= 0 && pos < text.length())
        {
            int i = 0;
            while (i + 1 < offsets.length && offsets[i + 1] <= pos)
                i++;
            return i;
        }
        else
        {
            return -1;
        }
    }
    
    public Box getBoxForPosition(int pos)
    {
        int i = getIndexForPosition(pos);
        return (i == -1) ? null : boxes.get(i);
    }
    
    public Rectangular getSubstringBounds(int spos, int epos)
    {
        if (spos >= 0 && spos < text.length() && epos >= 0 && epos <= text.length() && epos > spos)
        {
            //start and end index
            int bi1 = getIndexForPosition(spos);
            int ofs1 = spos - offsets[bi1];
            int bi2 = getIndexForPosition(epos - 1); //the start of the last char
            int ofs2 = epos - offsets[bi2];
            //find the bounds
            Rectangular r;
            if (bi1 == bi2) //within the same box
            {
                Box box = boxes.get(bi1);
                r = box.getSubstringBounds(ofs1, ofs2);
            }
            else //different boxes
            {
                Box box1 = boxes.get(bi1);
                Box box2 = boxes.get(bi2);
                r = box1.getSubstringBounds(ofs1, box1.getOwnText().length());
                Rectangular r2 = box2.getSubstringBounds(0, ofs2);
                if (r != null && r2 != null)
                {
                    if (r2.getX1() >= r.getX2() && r2.getY1() < r.getY2()) //basically on the same line
                    {
                        r.expandToEnclose(r2); //just expand the rectangle to cover both
                    }
                    else //different lines
                    {
                        if (boxes.isBlockLayout())
                        {
                            r.expandToEnclose(r2); //block - cover both
                        }
                        else
                        {
                            r.setX2(r.getX2() + r2.getWidth()); //inline - just expand the first box to indicate the total length
                        }
                    }
                }
            }
            return r;
        }
        else
            return null;
    }
    
    @Override
    public String toString()
    {
        return text;
    }

    //=================================================================

    private void computeOffsets()
    {
        StringBuilder sb = new StringBuilder();
        offsets = new int[boxes.size()];
        lengths = new int[boxes.size()];
        Box prev = null;
        int i = 0;
        for (Box box : boxes)
        {
            if (prev != null && boxesSeparated(prev, box))
                sb.append(' ');
            String btext = box.getOwnText(); 
            offsets[i] = sb.length();
            lengths[i] = btext.length();
            sb.append(btext);
            prev = box;
            i++;
        }
        text = sb.toString();
    }
    
    /**
     * Determines whether two subsequent boxes (on the same line) are separated by a space 
     * @param box1 the first box
     * @param box2 the second box
     * @return {@code true} when there is a space between the boxes
     */
    private boolean boxesSeparated(Box box1, Box box2)
    {
        Rectangular r1 = box1.getBounds();
        Rectangular r2 = box2.getBounds();
        if (AreaUtils.isOnSameLine(r1, r2)) //boxes on the same line
        {
            //determine whether there is a space between the boxes
            int minSep = Math.round(Math.max(box1.getTextStyle().getFontSize(), box2.getTextStyle().getFontSize()) * 0.4f); //at least 0.4em
            //compute the distance
            int dist = box2.getX1() - box1.getX2();
            return (dist >= minSep);
        }
        else
            return true; //not on the same line, always separated
    }
    
}
