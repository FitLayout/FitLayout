package cz.vutbr.fit.layout.bcs.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentImage;
import cz.vutbr.fit.layout.model.Rectangular;

public class AreaCreator
{
    /** Allowed box overlap in pixels for element boxes */
    private static final int ALLOWED_OVERLAP_ELEMENT = 1;
    /** Allowed box overlap in pixels for replaced content boxes */
    private static final int ALLOWED_OVERLAP_REPLACED = 1;
    /** Allowed box overlap in pixels for text boxes */
    private static final int ALLOWED_OVERLAP_TEXT = 2; //text lines sometimes overlap when line-height is too small
    
    private ArrayList<PageArea> areas;
    private HashSet<Integer> mask;
    private final int pageWidth;
    private final int pageHeight;

    private SpatialIndex areaTree;

    public AreaCreator(int w, int h)
    {
        this.pageWidth = w;
        this.pageHeight = h;
    }

    public List<PageArea> getAreas(Box root)
    {
        ArrayList<PageArea> ret;

        this.areaTree = new RTree();
        this.areaTree.init(null);
        this.areas = new ArrayList<>();
        this.mask = new HashSet<>();
        ret = new ArrayList<>();

        this.getAreasSubtree(root, Color.WHITE);

        for (int index = 0; index < this.areas.size(); index++)
        {
            if (this.mask.contains(index)) 
                continue;

            ret.add(this.areas.get(index));
        }

        Collections.sort(ret, new AreaSizeComparator());
        return ret;
    }

    private void getAreasSubtree(Box root, Color parentBg)
    {
        Color bgColor = this.getBgColor(root, parentBg);

        if (root.getChildCount() == 0)
        {
            /* No children */
            if (!this.isTransparent(root))
            {
                this.getArea(root, parentBg);
            }
            return;
        }
        else if (root.getChildCount() == 1)
        {
            // figure out if this line of "one child" continues to the bottom of tree
            final Box child = root.getChildAt(0);
            if (child.getType() == Type.TEXT_CONTENT)
            {
                this.getTextArea(child, bgColor);
                return;
            }
            else
            {
                if (this.hasNoBranches(child))
                {
                    this.getSmallestBox(root, parentBg);
                    return;
                }

                /* If it has branches, not much special to do - we still have to inspect all children */
            }
        }

        /* Recurse to the rest of the tree */
        for (Box child : root.getChildren())
        {
            if (child.getType() == Type.TEXT_CONTENT)
            {
                this.getTextArea(child, bgColor);
            }
            else if (child.getType() == Type.REPLACED_CONTENT)
            {
                this.getImageArea(child, child.getContentBounds());
            }
            else
            {
                this.getAreasSubtree(child, bgColor);
            }
        }
    }

    private boolean hasNoBranches(Box root)
    {
        if (root.getChildCount() == 0)
        {
            return true;
        }
        else if (root.getChildCount() == 1)
        {
            final Box child = root.getChildAt(0);
            if (child.getType() == Type.TEXT_CONTENT)
                return true;
            else
                return hasNoBranches(child);
        }
        else
        {
            return false;
        }
    }

    private void getSmallestBox(Box root, Color parentBg)
    {
        if (root.getChildCount() == 0)
        {
            /* No children - we have to return this one */
            this.getArea(root, parentBg);
        }
        else
        {
            final Box child = root.getChildAt(0);
            final Color bgColor = this.getBgColor(root, parentBg);
            if (child.getType() == Type.TEXT_CONTENT)
            {
                this.getTextArea(child, bgColor);
            }
            else if (child.getType() == Type.REPLACED_CONTENT)
            {
                this.getImageArea(child, child.getContentBounds());
            }
            else
            {
                if (this.isTransparent(root))
                {
                    getSmallestBox(child, parentBg);
                }
                else
                {
                    this.getArea(child, parentBg);
                }
            }
        }
    }

    private Color getBgColor(Box box, Color parentBg)
    {
        BufferedImage bgImg = getBackgroundImage(box);
        Color color = box.getBackgroundColor();

        if (color == null)
        { /* BG is transparent - use the color of the parent */
            color = parentBg;
        }

        if (bgImg != null)
        {
            AverageColor imgColor = new AverageColor(bgImg);
            if (imgColor.getColor() != null)
                return imgColor.mixWithBackground(color);
            else
                return color;
            /* DOC: mixing color of bg image with bg
             * - more precise -> if the bg is small compared to the box, it won't be so visual distinct
             * - also consider not mixing (original functionality)
             *   -> gives more distinct outline of the box
             *   -> even if small, the bg image may be used to visually higlight the box
             */
        }
        else
            return color;
    }

    private BufferedImage getBackgroundImage(Box box)
    {
        if (box.getBackgroundImagePng() != null)
        {
            try {
                final BufferedImage image = ImageIO.read(new ByteArrayInputStream(box.getBackgroundImagePng()));
                return image;
            } catch (IOException e) {
                return null;
            }
        }
        else
            return null;
    }
    
    private BufferedImage getContentImage(Box box)
    {
        if (box.getContentObject() != null && box.getContentObject() instanceof ContentImage)
        {
            final ContentImage img = (ContentImage) box.getContentObject();
            if (img.getPngData() != null)
            {
                try {
                    final BufferedImage image = ImageIO.read(new ByteArrayInputStream(img.getPngData()));
                    return image;
                } catch (IOException e) {
                    return null;
                }
            }
            else
                return null;
        }
        else
            return null;
    }
    
    private boolean isTransparent(Box box)
    {
        return !box.hasBackground();
    }

    private void getTextArea(Box box, Color bgColor)
    {
        /*Color color;
        float []hsb;
        float []bgHsb;
        int white_multiplier;
        int hsb_index;
        PageArea area;*/
        int white_multiplier;
        int hsb_index;
        Rectangular pos = box.getContentBounds();
        
        if (onPage(pos))
        {
            Color color = box.getColor();
            float[] hsb = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    
            /* DOC: white and grey need special treatment */
            if (hsb[1] == 0)
            {
                if (hsb[2] == 1)
                {
                    /* The text is white, we want to get the color of background ... */
    
                    float[] bgHsb = java.awt.Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
                    hsb[0] = bgHsb[0];
    
                    /* ... we want to slightly modify the initial value (so bold can be actually emphasized) ... */
                    hsb[1] = (float)0.2;
    
                    /* ... we want to modify saturation ... */
                    hsb_index = 1;
                    /* ... and we want to subtract from it for emphasis */
                    white_multiplier = -1;
                }
                else
                {
                    /* The text is grey - we want to modify brightness ... */
                    hsb_index = 2;
                    /* ... and we want to subtract from it for emphasis ... */
                    white_multiplier = -1;
    
                    if (hsb[2] == 0)
                    {
                        /* The color is black, set the initial value higher so bold can be actually emphasized */
                        hsb[2] = (float)0.2;
                    }
                }
            }
            else
            {
                /* The text colored - we want to modify saturation ... */
                hsb_index = 1;
                /* ... and we want to add to it for emphasis */
                white_multiplier = 1;
            }
    
            if (box.getTextStyle().getUnderline() > 0.5f) //underlined
            {
                hsb[hsb_index] += white_multiplier * 0.2f;
            }
            if (box.getTextStyle().getFontStyle() > 0.5f) //italics
            {
                hsb[hsb_index] -= white_multiplier * 0.2f;
            }
            if (box.getTextStyle().getFontWeight() > 0.5f) //bold
            {
                hsb[hsb_index] += white_multiplier * 0.3f;
            }
    
            if (hsb[hsb_index] > 1.0f)
                hsb[hsb_index] = 1.0f;
            else if (hsb[hsb_index] < 0.0)
                hsb[hsb_index] = 0.0f;
    
            final Color avgcolor = new Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
            final PageArea area = new PageArea(avgcolor, pos);
            area.setNode(box);
            this.addArea(area, ALLOWED_OVERLAP_TEXT);
        }
    }


    private void getImageArea(Box box, Rectangular pos)
    {
        if (onPage(pos))
        {
            final BufferedImage img = getContentImage(box);
            if (img != null && img.getWidth() > 0 && img.getHeight() > 0)
            {
                AverageColor avg = new AverageColor(img);
                if (avg.getColor() != null)
                {
                    PageArea area = new PageArea(avg.getColor(), pos);
                    area.setNode(box);
                    this.addArea(area, ALLOWED_OVERLAP_REPLACED);
                }
            }
        }
    }

    private void getArea(Box box, Color parentBg)
    {
        Rectangular rect = box.getContentBounds();
        if (onPage(rect))
        {
            final Color c = this.getBgColor(box, parentBg);
            if (c != null)
            {
                PageArea area = new PageArea(c, rect);
                area.setNode(box);
                this.addArea(area, ALLOWED_OVERLAP_ELEMENT);
            }
        }
    }

    private boolean onPage(Rectangular r)
    {
        if (r.getX1() > this.pageWidth || r.getY1() > this.pageHeight)
            return false;
        if (r.isEmpty())
            return false;
        if (r.getX2() < 0 || r.getY2() < 0)
            return false;
        return true;
    }

    private void addArea(PageArea area, int allowedOverlap) 
    {
        //allow <T>px overlaps for intersection detection
        Rectangle areaRect = new Rectangle(area.getLeft() + allowedOverlap, area.getTop() + allowedOverlap,
                area.getRight() - allowedOverlap, area.getBottom() - allowedOverlap);
        //detect overlaps
        AreaMatch match = new AreaMatch();
        this.areaTree.intersects(areaRect, match);
        for (Integer id: match.getIds())
        {
            this.mask.add(id);
        }

        this.areas.add(area);
        this.areaTree.add(area.getRectangle(), this.areas.size()-1);
    }
}
