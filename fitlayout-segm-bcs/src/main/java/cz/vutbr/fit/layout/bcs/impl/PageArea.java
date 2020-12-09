package cz.vutbr.fit.layout.bcs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infomatiq.jsi.Rectangle;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Rectangular;

public class PageArea
{
    private Integer id;

    private Color color;
    private int left;
    private int right;
    private int top;
    private int bottom;

    private PageArea parent;
    private final ArrayList<PageArea> children;
    private final HashMap<PageArea, PageAreaRelation> neighbors;
    /* This is a mean of neighbor distances */
    private int meanNeighborDistance;
    private int maxNeighborDistance;

    private Rectangle rectangle;
    private int vEdgeCount;
    private int hEdgeCount;

    private Box node;

    public static final int ALIGNMENT_NONE = 0;
    public static final int ALIGNMENT_LINE = 1;
    public static final int ALIGNMENT_COLUMN = 2;

    public static final int ALIGNMENT_LEFT = 1;
    public static final int ALIGNMENT_RIGHT = 2;
    public static final int ALIGNMENT_TOP = 4;
    public static final int ALIGNMENT_BOTTOM = 8;

    public static final int SHAPE_BLOB = 0;
    public static final int SHAPE_COLUMN = 1;
    public static final int SHAPE_ROW = 2;

    public static final double MAX_DIFF_RGB = 1.7320508075688772;
    public static final double MAX_DIFF_LAB = 258.68384120267046;
    public static final double MAX_DIFF_LCH = 149.93691702034678;

    public PageArea(Color c, Rectangular rect)
    {
        this.color = c;
        this.left = rect.getX1();
        this.top = rect.getY1();
        this.right = rect.getX2();
        this.bottom = rect.getY2();
        this.children = new ArrayList<>();
        this.neighbors = new HashMap<>();
        this.maxNeighborDistance = 0;
        this.meanNeighborDistance = 0;
        this.rectangle = null;
        this.vEdgeCount = 0;
        this.hEdgeCount = 0;
        this.id = null;
        this.node = null;
    }

    public PageArea(PageArea a)
    {
        this(a, false);
    }

    public PageArea(PageArea a, boolean inheritChildren)
    {
        this.color = new Color(a.color.getRGB());
        this.left = a.left;
        this.right = a.right;
        this.top = a.top;
        this.bottom = a.bottom;
        this.vEdgeCount = a.vEdgeCount;
        this.hEdgeCount = a.hEdgeCount;
        this.children = new ArrayList<>();
        this.neighbors = new HashMap<>();
        this.maxNeighborDistance = 0;
        this.meanNeighborDistance = 0;
        this.id = null;
        this.node = null;

        if (inheritChildren)
        {
            /* We don't want to change the relationships, just transfer the references */
            this.children.addAll(a.getChildren());
        }
    }

    public Integer getId()
    {
        return this.id;
    }

    public void setId(int top, int left)
    {
        // TODO: this can cause problems if the page is wider than 9999px
        this.id = (top * 10000 + left);
    }

    public void calculateId()
    {
        this.setId(this.top, this.left);
    }

    public boolean contains(PageArea obj)
    {
        return this.left <= obj.left &&
               this.right >= obj.right &&
               this.top <= obj.top &&
               this.bottom >= obj.bottom;
    }

    public boolean overlaps(PageArea obj)
    {
        return this.right >= obj.left && this.left <= obj.right && this.bottom >= obj.top && this.top <= obj.bottom;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public int getLeft()
    {
        return left;
    }

    public void setLeft(int left)
    {
        this.left = left;
        this.rectangle = null;
    }

    public int getRight()
    {
        return right;
    }

    public void setRight(int right)
    {
        this.right = right;
        this.rectangle = null;
    }

    public int getTop()
    {
        return top;
    }

    public void setTop(int top)
    {
        this.top = top;
        this.rectangle = null;
    }

    public int getBottom()
    {
        return bottom;
    }

    public void setBottom(int bottom)
    {
        this.bottom = bottom;
        this.rectangle = null;
    }

    public int getWidth()
    {
        return this.right - this.left + 1;
    }

    public int getHeight()
    {
        return this.bottom - this.top + 1;
    }

    @Override
    public String toString()
    {
        return "("+this.getTop()+","+this.getLeft()+"-"+this.getWidth()+"x"+this.getHeight()+")";
    }

    public void addChild(PageArea child)
    {
        this.addChild(child, false);
    }

    public PageArea tryAdd(PageArea a)
    {
        PageArea ret = new PageArea(this);
        ret.addChild(a, true);
        return ret;
    }

    public void addChild(PageArea child, boolean tryout)
    {
        this.children.add(child);
        if (!tryout)
        {
            child.setParent(this);
        }

        if (child.getBottom() > this.getBottom()) this.setBottom(child.getBottom());
        if (child.getRight() > this.getRight()) this.setRight(child.getRight());
        if (child.getLeft() < this.getLeft()) this.setLeft(child.getLeft());
        if (child.getTop() < this.getTop()) this.setTop(child.getTop());
    }

    public void delChild(PageArea child)
    {
        // TODO: adjust borders, pattern and reset the rectangle
        this.children.remove(child);
    }

    public List<PageArea> getChildren()
    {
        return this.children;
    }


    public void reclaimChildren()
    {
        if (this.children.size() == 0)
        {
            this.setParent(null);
        }
        else
        {
            for (PageArea child: this.children)
            {
                child.setParent(this);
            }
        }
    }

    public void giveUpChildren()
    {
        for (PageArea child: this.getChildren())
        {
            /* We need to return all areas that have been added to
             * the tmpGroup (not those inherited from the original group)
             * to the pool */
            if (child.getParent() == this)
            {
                child.setParent(null);
            }
        }
    }

    public int getAreaCount()
    {
        if (this.children.size() > 0)
        {
            return this.children.size();
        }
        else
        {
            return 1;
        }
    }

    public void mergeWith(PageArea a)
    {
        if (a.getChildren().size() > 0)
        {
            for (PageArea child: a.getChildren())
            {
                this.addChild(child);
            }
        }
        else
        {
            this.addChild(a);
        }
    }

    public void setParent(PageArea parent)
    {
        this.parent = parent;
    }

    public PageArea getParent()
    {
        return this.parent;
    }

    public HashMap<PageArea, PageAreaRelation> getNeighbors()
    {
        return this.neighbors;
    }

    public PageAreaRelation getNeighbor(PageArea a)
    {
        return this.neighbors.get(a);
    }

    public void addNeighbor(PageArea a, int direction, int cardinality)
    {
        PageAreaRelation neighbor;
        int distance;

        distance = this.getDistanceAbsolute(a);

        if (this.neighbors.containsKey(a))
        {
            neighbor = this.neighbors.get(a);
            if (distance < neighbor.getSimilarity())
            {
                neighbor.setSimilarity(distance);
                neighbor.setAbsoluteDistance(distance);
                neighbor.setCardinality(neighbor.getCardinality()+cardinality);
            }
        }

        neighbor = new PageAreaRelation(this, a, distance, direction);
        neighbor.setCardinality(cardinality);
        neighbor.setAbsoluteDistance(distance);

        this.neighbors.put(a, neighbor);
        a.neighbors.put(this, neighbor);
    }

    public void addNeighbor(PageAreaRelation rel)
    {
        PageArea a;
        int distance;

        if (rel.getA() == this) a = rel.getB();
        else if (rel.getB() == this) a = rel.getA();
        else return;

        if (this.neighbors.containsKey(a)) return;

        distance = this.getDistanceAbsolute(a);

        PageAreaRelation neighbor = new PageAreaRelation(this, a, distance, rel.getDirection());
        neighbor.setCardinality(rel.getCardinality());
        neighbor.setAbsoluteDistance(distance);

        this.neighbors.put(a, neighbor);
        a.neighbors.put(this, neighbor);
    }

    public void delNeighbor(PageArea a)
    {
        PageAreaRelation rel = this.neighbors.get(a);

        if (rel == null) return;

        this.neighbors.remove(a);
        a.neighbors.remove(this);
    }

    public int getMaxNeighborDistance()
    {
        return maxNeighborDistance;
    }

    public void setMaxNeighborDistance(int maxNeighborDistance)
    {
        this.maxNeighborDistance = maxNeighborDistance;
    }

    public int getMeanNeighborDistance()
    {
        return meanNeighborDistance;
    }

    public void setMeanNeighborDistance(int meanNeighborDistance)
    {
        this.meanNeighborDistance = meanNeighborDistance;
    }

    public void calculateNeighborDistances()
    {
        int cnt = 0, sum = 0, val;
        this.maxNeighborDistance = 0;
        for (Map.Entry<PageArea, PageAreaRelation> entry : this.neighbors.entrySet())
        {
            val = entry.getValue().getAbsoluteDistance();
            if (val > this.maxNeighborDistance) this.maxNeighborDistance = val;
            sum += val;
            cnt++;
        }

        this.meanNeighborDistance = sum/((cnt!=0)?cnt:1);
    }

    public double getSimilarity(PageArea a, int alignmentScore)
    {
        double shape = getShapeSimilarity(a);
        double color = getColorSimilarity(a);
//        double position = getDistance(a);
        double position = getDistanceNeighbor(a);

        if (position == 0.0)
        {
            return 0.0;
        }
        else if (position > 1.0)
        {
            return 1.0;
        }

//        shape = 0;
        return (0.3*shape + 0.5*color + 0.2*position)/(2*alignmentScore);
    }

    public double getSizeSimilarity(PageArea a)
    {
        double widthRatio;
        double heightRatio;

        if (this == a) return 0;

        /* DOC: size similarity is not used currently */
        /* DOC: size similarity has to be counted separately for width and height and the better
         * value should be then used - if two boxes are size-similar, they will usually have the
         * same size only in one direction, the difference in the other direction one might be
         * significantly higher */

        widthRatio = (double)Math.abs(this.getWidth()-a.getWidth())/(this.getWidth()+ a.getWidth());
        heightRatio = (double)Math.abs(this.getHeight()-a.getHeight())/(this.getHeight() + a.getHeight());

        return Math.min(widthRatio, heightRatio);
    }

    public double getShapeSimilarity(PageArea a)
    {
        int surface1, surface2;
        double ratio1, ratio2;
        double ratioMin, ratioMax;
        double ratioSimilarity;
        double surfaceSimilarity;

        ratio1 = (double)this.getWidth()/this.getHeight();
        ratio2 = (double)a.getWidth()/a.getHeight();
        ratioMin = Math.min(ratio1, ratio2);
        ratioMax = Math.max(ratio1, ratio2);
        surface1 = this.getWidth()*this.getHeight();
        surface2 = a.getWidth()*a.getHeight();

        if (ratio1 == ratio2) {
            /* Workaround for Java's 0/0 = NaN */
            ratioSimilarity = 0;
        } else {
            ratioSimilarity = (ratioMax-ratioMin)/((ratioMax*ratioMax-1)/ratioMax);
        }

        if (surface1 == surface2) {
            /* Workaround for Java's 0/0 = NaN */
            surfaceSimilarity = 0;
        } else {
            surfaceSimilarity = 1-Math.min(surface1, surface2)/Math.max(surface1, surface2);
        }

        return (ratioSimilarity+surfaceSimilarity)/2;
    }

    public double getColorSimilarity(PageArea a)
    {
        double colorDistance;

        if (this == a) return 0;

        /* DOC: we are using RGB color diff
         * - Lab and LCH color spaces operate with the fact that human eye is
         *   more sensitive to brightness than to color hue
         * - however our assumption is that creators of the web use more color hue
         *   to distinguish different (unrelated) elements on the page
         *   This has also been experimentally evaluated - RGB color diff gave better
         *   results when used for element clustering
         * good reference book for color spaces: Understanding Color Management
         */
        colorDistance = colorDiffRgb(this.getColor(), a.getColor());
        colorDistance /= MAX_DIFF_RGB;

        return colorDistance;
    }

    public double getDistance(PageArea a)
    {
        int horizontalDistance;
        int verticalDistance;
        int width;
        int height;
        int top, bottom, left, right;

        if (this == a || this.overlaps(a)) return 0;

        /* DOC: Position distance: 0 - 1 */
        top = Math.min(this.top, a.top);
        bottom = Math.max(this.bottom, a.bottom);
        height = bottom-top;

        left = Math.min(this.left, a.left);
        right = Math.max(this.right, a.right);
        width = right-left;


        if (this.getAlignment(a) == ALIGNMENT_COLUMN)
        {
            /* DOC: this is important - if one area is smaller and doesn't
             * extend over larger's area left/right, it is considered to be
             * horizontally aligned */
            horizontalDistance = 0;
        }
        else
        {
            int ll, lr, rr, rl;
            ll = Math.abs(a.left-this.left);
            lr = Math.abs(a.left-this.right);
            rr = Math.abs(a.right-this.right);
            rl = Math.abs(a.right-this.left);
            horizontalDistance = Math.min(Math.min(ll, lr), Math.min(rl, rr));
            if (horizontalDistance > 0) horizontalDistance--; /* DOC: We want to get just the space between, subtract one border */
        }

        if (this.getAlignment(a) == ALIGNMENT_LINE)
        {
            /* DOC: this is important - if one area is smaller and doesn't
             * extend over larger's area top/bottom, it is considered to be
             * vertically aligned */
            verticalDistance = 0;
        }
        else
        {
            int tt, tb, bb, bt;
            tt = Math.abs(a.top-this.top);
            tb = Math.abs(a.top-this.bottom);
            bb = Math.abs(a.bottom-this.bottom);
            bt = Math.abs(a.bottom-this.top);
            verticalDistance = Math.min(Math.min(tt, tb), Math.min(bt, bb));
            if (verticalDistance > 0) verticalDistance--; /* DOC: We want to get just the space between, subtract one border */
        }


        /* DOC: this formula will be important to document */
        return Math.sqrt(verticalDistance*verticalDistance+horizontalDistance*horizontalDistance)/
               Math.sqrt(width*width+height*height);
    }

    public double getDistanceNeighbor(PageArea a)
    {
        PageAreaRelation rel = this.neighbors.get(a);
        double forward, backward;
        double dist;

        if (rel == null) return 1.1;

        if (this == a || this.overlaps(a)) return 0.0;

//        if (this.maxNeighborDistance == this.meanNeighborDistance ||
//            a.maxNeighborDistance == a.meanNeighborDistance) return 0.0;

        dist = this.getDistanceAbsolute(a);
        forward = dist/this.maxNeighborDistance;
        backward = dist/a.maxNeighborDistance;
        return (forward+backward)/2;
    }

    public int getDistanceAbsolute(PageArea a)
    {
        if (this == a || this.overlaps(a)) return 0;

        if (this.left > a.right || a.left > this.right)
        {
            return Math.min(Math.abs(this.left-a.right), Math.abs(a.left-this.right));
        }
        else
        {
            return Math.min(Math.abs(this.top-a.bottom), Math.abs(a.top-this.bottom));
            //  if (this.top > a.bottom || a.top > this.bottom)
            // -> this condition is now implicit
        }
    }

    public int getAlignment(PageArea a)
    {
        if (this.bottom >= a.top && this.top <= a.bottom) return ALIGNMENT_LINE;
        else if (this.right >= a.left && this.left <= a.right) return ALIGNMENT_COLUMN;
        else if (a.getLeft() == this.getLeft()) return ALIGNMENT_COLUMN;
        else if (a.getTop() == this.getTop()) return ALIGNMENT_LINE;
        else if (a.getRight() == this.getRight()) return ALIGNMENT_COLUMN;
        else if (a.getBottom() == this.getBottom()) return ALIGNMENT_LINE;

        else return ALIGNMENT_NONE;
    }

    public int getSideAlignment(PageArea a)
    {
        if (a.getLeft() == this.getLeft()) return ALIGNMENT_LEFT;
        else if (a.getTop() == this.getTop()) return ALIGNMENT_TOP;
        else if (a.getRight() == this.getRight()) return ALIGNMENT_RIGHT;
        else if (a.getBottom() == this.getBottom()) return ALIGNMENT_BOTTOM;

        else return ALIGNMENT_NONE;
    }

    public static double colorDiff(Color a, Color b)
    {
        /* DOC: max value Lab: 258.68384120267046 - 0,0,255 - 0,255,0 */
        /* DOC: computing color difference according to CIE94, threshold value is 2.3 for not noticeable color diff */
        /* DOC: in the end I am using CIE76, it turned out to be more accurate for our purpose (empirically verified), CIE94 is commented out */
        /* DOC: we will probably use higher threshold because we want also similar colors (with noticeable diff) to be merged */
        /* DOC: see http://en.wikipedia.org/wiki/Color_difference for details */
        double []lab;
        double l1, a1, b1;
        double l2, a2, b2;

        // just return some high value to indicate a border of an area
        if (a == null || b == null) return 100;

        lab = rgbToLab(a.getRed(), a.getGreen(), a.getBlue());
        l1 = lab[0]; a1 = lab[1]; b1 = lab[2];
        lab = rgbToLab(b.getRed(), b.getGreen(), b.getBlue());
        l2 = lab[0]; a2 = lab[1]; b2 = lab[2];

        return Math.sqrt(Math.pow(l2-l1, 2)+Math.pow(a2-a1, 2)+Math.pow(b2-b1, 2));
    }

    public static double colorDiffLch(Color a, Color b)
    {
        /* DOC: max value LCH: 149.93691702034678 - 255,255,255 - 0,0,255 */
        /* DOC: computing color difference according to CIE94, threshold value is 2.3 for not noticeable color diff */
        /* DOC: in the end I am using CIE76, it turned out to be more accurate for our purpose (empirically verified), CIE94 is commented out */
        /* DOC: we will probably use higher threshold because we want also similar colors (with noticeable diff) to be merged */
        /* DOC: see http://en.wikipedia.org/wiki/Color_difference for details */
        double []lab;
        double []lch;
        double l1, c1, h1;
        double l2, c2, h2;
        double dl, dc, dh;

        // just return some high value to indicate a border of an area
        if (a == null || b == null) return 100;

        lab = rgbToLab(a.getRed(), a.getGreen(), a.getBlue());
        lch = labToLch(lab);
        l1 = lch[0]; c1 = lch[1]; h1 = lch[2];
        lab = rgbToLab(b.getRed(), b.getGreen(), b.getBlue());
        lch = labToLch(lab);
        l2 = lch[0]; c2 = lch[1]; h2 = lch[2];

        dl = l2 - l1;
        dc = c2 - c1;
        dc /= (1+0.045*c1);
        dh = h2 - h1;
        dh /= (1+0.015*c1);

        return Math.sqrt(dl*dl + dc*dc + dh*dh);
    }

    public static double colorDiffRgb(Color color1, Color color2)
    {
        /* DOC: max value RGB: 1.7320508075688772 - 0,0,0 - 255,255,255 */
        double r1, g1, b1;
        double r2, g2, b2;
        double dr, dg, db;

        r1 = (double)color1.getRed()/255;
        g1 = (double)color1.getGreen()/255;
        b1 = (double)color1.getBlue()/255;
        r2 = (double)color2.getRed()/255;
        g2 = (double)color2.getGreen()/255;
        b2 = (double)color2.getBlue()/255;

        dr = r2-r1;
        dg = g2-g1;
        db = b2-b1;

        return Math.sqrt(dr*dr + dg*dg + db*db);
    }

    private static double[] rgbToLab(int R, int G, int B)
    {
        /* DOC: http://www.colourphil.co.uk/lab_lch_colour_space.html */
        double whiteX = 0.9505;
        double whiteY = 1.0;
        double whiteZ = 1.0888;

        double r, g, b;
        r = (double)R/255;
        g = (double)G/255;
        b = (double)B/255;

        // TODO: this is not verified
        // source: http://cookbooks.adobe.com/post_Useful_color_equations__RGB_to_LAB_converter-14227.html
        if (r > 0.04045){ r = Math.pow((r + 0.055) / 1.055, 2.4); }
        else { r = r / 12.92; }
        if ( g > 0.04045){ g = Math.pow((g + 0.055) / 1.055, 2.4); }
        else { g = g / 12.92; }
        if (b > 0.04045){ b = Math.pow((b + 0.055) / 1.055, 2.4); }
        else {  b = b / 12.92; }

        // DOC: we are assuming sRGB -> XYZ (sRGB source model sounds like the best option)
        double x, y, z;
        x = 0.4124564*r + 0.3575761*g + 0.1804375*b;
        y = 0.2126729*r + 0.7151522*g + 0.0721750*b;
        z = 0.0193339*r + 0.1191920*g + 0.9503041*b;

        // XYZ -> Lab
        double xr, yr, zr;
        double fx, fy, fz;
        xr = x/whiteX;
        yr = y/whiteY;
        zr = z/whiteZ;
        fx = (xr>0.008856)?cubeRoot(xr):computeF(xr);
        fy = (yr>0.008856)?cubeRoot(yr):computeF(yr);
        fz = (zr>0.008856)?cubeRoot(zr):computeF(zr);

        double []lab = new double [3];
        lab[0] = 116*fy-16;
        lab[1] = 500*(fx-fy);
        lab[2] = 200*(fy-fz);

        return lab;
    }

    private static double computeF(double x)
    {
        return (903.3*x+16)/116;
    }

    private static double cubeRoot(double x) {
        return Math.pow(x, 1.0/3);
    }

    private static double[] labToLch(double []lab)
    {
        /* DOC: http://www.brucelindbloom.com/index.html?Eqn_Lab_to_LCH.html */
        double []lch = new double[3];

        lch[0] = lab[0];
        lch[1] = Math.sqrt(lab[1]*lab[1]+lab[2]*lab[2]);
        lch[2] = Math.atan2(lab[2], lab[1]);

        return lch;
    }

    public Rectangle getRectangle()
    {
        if (this.rectangle == null)
        {
            this.rectangle = new Rectangle(this.left, this.top, this.right, this.bottom);
        }

        return this.rectangle;
    }

    public void resetRectangle()
    {
        this.rectangle = null;
    }

    public int getVEdgeCount()
    {
        return vEdgeCount;
    }

    public double getVRatio()
    {
        int edgeCount;
        if (this.vEdgeCount == 0) return 0.1;
        else
        {
            edgeCount = this.vEdgeCount+this.hEdgeCount;
            if (edgeCount == 0) return 0;
            else return this.vEdgeCount/edgeCount;
        }
    }

    public void setVEdgeCount(int vEdgeCount)
    {
        this.vEdgeCount = vEdgeCount;
    }

    public void addVEdgeCount(int vEdgeCount)
    {
        this.vEdgeCount = vEdgeCount;
    }

    public int getHEdgeCount()
    {
        return hEdgeCount;
    }

    public double getHRatio()
    {
        int edgeCount;
        if (this.hEdgeCount == 0) return 0.1;
        else
        {
            edgeCount = this.vEdgeCount+this.hEdgeCount;
            if (edgeCount == 0) return 0;
            else return this.hEdgeCount/edgeCount;
        }
    }

    public void setHEdgeCount(int hEdgeCount)
    {
        this.hEdgeCount = hEdgeCount;
    }

    public void addHEdgeCount(int hEdgeCount)
    {
        this.hEdgeCount = hEdgeCount;
    }

    public boolean isRow()
    {
        if (this.getHRatio() > 2*this.getVRatio()) return true;
        else return false;
    }

    public boolean isColumn()
    {
        if (this.getHRatio() < 0.5*this.getVRatio()) return true;
        else return false;
    }

    public int getShape()
    {
        if (this.isRow()) return SHAPE_ROW;
        else if (this.isColumn()) return SHAPE_COLUMN;
        else return SHAPE_BLOB;
    }

    public boolean isBlob()
    {
        return !(this.isColumn() || this.isRow());
    }

    public Box getNode()
    {
        return node;
    }

    public void setNode(Box node)
    {
        this.node = node;
    }
}
