/**
 * BoxNode.java
 *
 * Created on 2.6.2006, 11:39:46 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.ReplacedContent;
import org.fit.cssbox.layout.ReplacedImage;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cz.vutbr.fit.layout.impl.DefaultBox;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Border.Style;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.CSSProperty.BorderStyle;

/**
 * A node of a tree of visual blocks.
 * 
 * @author burgetr
 */
public class BoxNode extends DefaultBox
{
    /** The CSSBox box that forms this node */
    protected Box box;
    
    /** The transformation that should be applied to the box */
    protected BoxTransform transform;
    
    /** Zoom relative to original box sizes */
    private float zoom;
    
    //===================================================================================
    
    /**
     * Creates a new node containing a box with a transparent background.
     * 
     * @param box the contained box
     * @param pageIri containing page IRI
     * @param zoom zoom factor to apply
     */
    public BoxNode(Box box, IRI pageIri, float zoom)
    {
        this(box, pageIri, null, zoom);
    }
    
    /**
     * Creates a new node containing a box with a computed background. The background
     * is computed separately when creating the nodes because the Viewport (and some
     * table elements) are treated in a special way.
     * 
     * @param box the contained box
     * @param pageIri containing page IRI
     * @param bgColor computed backgound color to be used for the box
     * @param zoom zoom factor to apply
     */
    public BoxNode(Box box, IRI pageIri, Color bgColor, float zoom)
    {
        super();
        this.box = box;
        this.zoom = zoom;
        setBackgroundColor(bgColor);
        setPageIri(pageIri);
        //copy the bounds from the box
        if (box != null)
        {
            loadBoxProperties();
            transform = new BoxTransform(box);
        }
    }

    @Override
    public String toString()
    {
        Box box = getBox();
        String ret = "";
        /*if (efficientBackground != null)
            ret += (box != null && isVisuallySeparated()) ? "+" : "-";*/
        ret += getOrder() + ": ";
        if (box == null)
            ret += "- empty -";
        else if (box instanceof Viewport)
            ret += box.toString();
        else if (box instanceof ElementBox)
        {
            ElementBox elem = (ElementBox) box;
            ret += elem.getElement().getTagName();
            ret += " [" + elem.getElement().getAttribute("id") + "]";
            ret += " [" + elem.getElement().getAttribute("class") + "]";
            ret += " B" + getBounds().toString();
            ret += " V" + getVisualBounds().toString();
        }
        else if (box instanceof TextBox)
        {
            ret = ((TextBox) box).getText();
            ret += " (" + box.getAbsoluteBounds().x + ","
            			+ box.getAbsoluteBounds().y + ","
            			+ (box.getAbsoluteBounds().x + box.getAbsoluteBounds().width - 1) + ","
            			+ (box.getAbsoluteBounds().y + box.getAbsoluteBounds().height - 1) + ")";
        }
        else
            ret = "?: " + box.toString();
        
        return ret;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BoxNode)
        {
            return ((BoxNode) obj).getBox() == getBox();
        }
        else
            return false;
    }
    
    @Override
    protected void childrenChanged()
    {
        recomputeTextStyle();
    }

    //===================================================================================

    /**
     * Loads the intrinsic box properties obtained from CSSBox.
     */
    private void loadBoxProperties()
    {
        setType(getIntrinsicType());
        setBounds(new Rectangular(getIntrinsicBounds()));
        setContentBounds(new Rectangular(getIntrinsicBounds()));
        setOwnText(recursiveGetText(this));
        for (Border.Side side : Border.Side.values())
        {
            setBorderStyle(side, getIntrinsicBorderStyle(side));
        }
        setColor(getIntrinsicColor());
        setFontFamily(getIntrinsicFontFamily());
        setContentObject(getIntrinsicContentObject());
        
        //initially, the box is considered to be background-separated if it has a declared background
        //later this is recomputed when the box tree is built and the efficient backgrounds are
        //computed
        setBackgroundSeparated((box instanceof ElementBox && ((ElementBox) box).getBgcolor() != null));
    }
    
    @Override
    public Rectangular getIntrinsicBounds()
    {
        final Box box = getBox();
        Rectangular ret = null;
        
        if (box instanceof Viewport)
        {
            ret = new RectangularZ(((Viewport) box).getClippedBounds(), zoom);
        }
        else if (box instanceof ElementBox)
        {
            final ElementBox elem = (ElementBox) box;
            ret = new RectangularZ(elem.getAbsoluteBorderBounds().intersection(elem.getClipBlock().getClippedContentBounds()), zoom);
        }
        else //not an element
        {
            ret = new RectangularZ(box.getAbsoluteBounds().intersection(box.getClipBlock().getClippedContentBounds()), zoom);
        }
        
        return ret;
    }

    
    /**
     * Computes node the content bounds. They correspond to the background bounds
     * however, when a border is present, it is included in the contents. Moreover,
     * the box is clipped by its clipping box.
     */
    private Rectangular computeContentBounds()
    {
        Box box = getBox();
        Rectangular ret = null;
        
        if (box instanceof Viewport)
        {
            ret = new RectangularZ(((Viewport) box).getClippedBounds(), zoom);
        }
        else if (box instanceof ElementBox)
        {
            ElementBox elem = (ElementBox) box;
            //at least one border - take the border bounds
            //TODO: when only one border is present, we shouldn't take the whole border box? 
            if (elem.getBorder().top > 0 || elem.getBorder().left > 0 ||
                elem.getBorder().bottom > 0 || elem.getBorder().right > 0)
            {
                ret = new RectangularZ(elem.getAbsoluteBorderBounds(), zoom);
            }
            //no border
            else
            {
                ret = new RectangularZ(elem.getAbsolutePaddingBounds(), zoom);
            }
        }
        else //not an element - return the whole box
            ret = new RectangularZ(box.getAbsoluteBounds(), zoom);

        //clip with the clipping bounds
        if (box.getClipBlock() != null)
        {
            Rectangular clip = new RectangularZ(box.getClipBlock().getClippedContentBounds(), zoom);
            ret = ret.intersection(clip);
        }
        
        return ret;
    }
    
    public int getIntrinsicTopBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return zoom(((ElementBox) box).getBorder().top);
        else
            return 0;
    }

    public int getIntrinsicBottomBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return zoom(((ElementBox) box).getBorder().bottom);
        else
            return 0;
    }

    public int getIntrinsicLeftBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return zoom(((ElementBox) box).getBorder().left);
        else
            return 0;
    }

    public int getIntrinsicRightBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return zoom(((ElementBox) box).getBorder().right);
        else
            return 0;
    }

    public Border getIntrinsicBorderStyle(Side side)
    {
        Box box = getBox();
        if (box instanceof ElementBox)
        {
            final NodeData style = ((ElementBox) box).getStyle();
            TermColor tclr = style.getValue(TermColor.class, "border-"+side+"-color");
            CSSProperty.BorderStyle bst = style.getProperty("border-"+side+"-style");
            if (bst == null)
                bst = BorderStyle.NONE;
            
            Color clr = null;
            if (tclr != null)
                clr = Units.toColor(tclr.getValue());
            if (clr == null)
            {
                clr = Units.toColor(box.getVisualContext().getColor());
                if (clr == null)
                    clr = Color.BLACK;
            }

            int rwidth = 0;
            switch (side)
            {
                case BOTTOM:
                    rwidth = getIntrinsicBottomBorder();
                    break;
                case LEFT:
                    rwidth = getIntrinsicLeftBorder();
                    break;
                case RIGHT:
                    rwidth = getIntrinsicRightBorder();
                    break;
                case TOP:
                    rwidth = getIntrinsicTopBorder();
                    break;
            }
            
            Border.Style rstyle;
            switch (bst)
            {
                case NONE:
                case HIDDEN:
                    rstyle = Style.NONE;
                    break;
                case DASHED:
                    rstyle = Style.DASHED;
                    break;
                case DOTTED:
                    rstyle = Style.DOTTED;
                    break;
                case DOUBLE:
                    rstyle = Style.DOUBLE;
                    break;
                default:
                    rstyle = Style.SOLID;
                    break;
            }
            
            return new Border(rwidth, rstyle, clr);
        }
        else
            return new Border();
    }

    //===================================================================================
    
    /**
     * @return the contained box
     */
    public Box getBox()
    {
        return box;
    }
    
    private String recursiveGetText(BoxNode root)
    {
        Box box = root.getBox();
        if (box instanceof TextBox)
            return ((TextBox) box).getText();
        else
        {
            String ret = "";
            for (int i = 0; i < root.getChildCount(); i++)
            {
                if (ret.trim().length() > 0)
                    ret += " ";
                ret = ret + recursiveGetText((BoxNode) root.getChildAt(i)).trim();
            }
            return ret;
        }
    }

    //==================================================================================
    
    private void recomputeTextStyle()
    {
        TextStyle textStyle = getTextStyle();
        if (textStyle == null)
        {
            textStyle = new TextStyle();
            setTextStyle(textStyle);
        }
        else
            textStyle.reset();
        
        if (isLeaf())
        {
            int len = getText().trim().length();
            textStyle.setFontSizeSum(getIntrinsicFontSize() * len);
            textStyle.setFontWeightSum(getIntrinsicFontWeight() * len);
            textStyle.setFontStyleSum(getIntrinsicFontStyle() * len);
            textStyle.setUnderlineSum(getIntrinsicUnderline() * len);
            textStyle.setLineThroughSum(getIntrinsicLineThrough() * len);
            textStyle.setContentLength(len);
        }
        else
        {
            for (cz.vutbr.fit.layout.model.Box box : getChildren())
            {
                textStyle.updateAverages(box.getTextStyle());
            }
        }
    }
    
    public float getIntrinsicFontSize()
    {
        return getBox().getVisualContext().getFontSize();
    }

    public float getIntrinsicFontStyle()
    {
        return getBox().getVisualContext().getFontInfo().isItalic() ? 1.0f : 0.0f;
    }

    public float getIntrinsicFontWeight()
    {
        return getBox().getVisualContext().getFontInfo().isBold() ? 1.0f : 0.0f;
    }

    public float getIntrinsicUnderline()
    {
        return getBox().getVisualContext().getTextDecoration().contains(CSSProperty.TextDecoration.UNDERLINE) ? 1.0f : 0.0f;
    }

    public float getIntrinsicLineThrough()
    {
        return getBox().getVisualContext().getTextDecoration().contains(CSSProperty.TextDecoration.LINE_THROUGH) ? 1.0f : 0.0f;
    }

    public Color getIntrinsicColor()
    {
        return Units.toColor(getBox().getVisualContext().getColor());
    }

    public String getIntrinsicFontFamily()
    {
        return getBox().getVisualContext().getFontInfo().getFamily();
    }

    public ContentObject getIntrinsicContentObject()
    {
        if (getBox().isReplaced())
        {
            ReplacedContent content = ((ReplacedBox) getBox()).getContentObj();
            if (content instanceof ReplacedImage)
                return new ContentImageImpl((ReplacedImage) content);
            else
                return null;
        }
        else
            return null;
    }

    public Type getIntrinsicType()
    {
        if (getBox().isReplaced())
            return Type.REPLACED_CONTENT;
        else if (getBox() instanceof TextBox)
            return Type.TEXT_CONTENT;
        else
            return Type.ELEMENT;
    }

    public Node getDOMNode()
    {
        return getBox().getNode();
    }
    
    @Override
    public int getSourceNodeId()
    {
        return System.identityHashCode(getBox().getNode());
    }

    @Override
    public String getTagName()
    {
        final Node node = getDOMNode();
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
            return ((Element) node).getTagName().toLowerCase();
        else
            return null;
    }

    @Override
    public String getAttribute(String name)
    {
        final Node node = getDOMNode();
        if (node != null)
        {
            if ("href".equals(name))
                return getAncestorAttribute(node, "a", name);
            else
                return getElementAttribute(node, name);
        }
        else
            return null;
    }

    @Override
    public Map<String, String> getAttributes()
    {
        final Node node = getDOMNode();
        NamedNodeMap map = null;
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            map = node.getAttributes();
        }
        else if (node.getNodeType() == Node.TEXT_NODE) //text nodes -- try parent //TODO how to propagate from ancestors correctly?
        {
            final Node pnode = node.getParentNode();
            if (pnode != null && pnode.getNodeType() == Node.ELEMENT_NODE)
            {
                map = pnode.getAttributes();
            }
        }
        
        Map<String, String> ret = new HashMap<>((map == null) ? 1 : (map.getLength() + 1));
        if (map != null) //store the attributes found
        {
            for (int i = 0; i < map.getLength(); i++)
            {
                final Node attr = map.item(i);
                ret.put(attr.getNodeName(), attr.getNodeValue());
            }
        }
        //eventually add the href value (which may be inherited from top)
        if (!ret.containsKey("href"))
        {
            String href = getAncestorAttribute(node, "a", "href");
            if (href != null)
                ret.put("href", href);
        }
        return ret;
    }

    protected String getElementAttribute(Node node, String attrName)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            final Element el = (Element) node;
            if (el.hasAttribute(attrName))
                return el.getAttribute(attrName);
            else
                return null;
        }
        else if (node.getNodeType() == Node.TEXT_NODE) //text nodes -- try parent //TODO how to propagate from ancestors correctly?
        {
            final Node pnode = node.getParentNode();
            if (pnode != null && pnode.getNodeType() == Node.ELEMENT_NODE)
            {
                final Element parent = (Element) pnode;
                if (parent.hasAttribute(attrName))
                    return parent.getAttribute(attrName);
                else
                    return null;
            }
            else
                return null;
        }
        else
            return null;
    }
    
    protected String getAncestorAttribute(Node node, String elementName, String attrName)
    {
        Node cur = node;
        //find the parent with the given name
        while (cur.getNodeType() != Node.ELEMENT_NODE || !elementName.equals(cur.getNodeName()))
        {
            cur = cur.getParentNode();
            if (cur == null)
                return null;
        }
        //read the attribute
        final Element el = (Element) cur;
        if (el.hasAttribute(attrName))
            return el.getAttribute(attrName);
        else
            return null;
    }
    
    @Override
    public DisplayType getDisplayType()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
        {
            CSSProperty.Display display = ((ElementBox) box).getDisplay();
            if (display == null)
                return DisplayType.BLOCK; //e.g. the viewport has no display value
            switch (display)
            {
                case BLOCK:
                    return DisplayType.BLOCK;
                case INLINE:
                    return DisplayType.INLINE;
                case INLINE_BLOCK:
                    return DisplayType.INLINE_BLOCK;
                case INLINE_TABLE:
                    return DisplayType.INLINE_TABLE;
                case LIST_ITEM:
                    return DisplayType.LIST_ITEM;
                case NONE:
                    return DisplayType.NONE;
                case RUN_IN:
                    return DisplayType.RUN_IN;
                case TABLE:
                    return DisplayType.TABLE;
                case TABLE_CAPTION:
                    return DisplayType.TABLE_CAPTION;
                case TABLE_CELL:
                    return DisplayType.TABLE_CELL;
                case TABLE_COLUMN:
                    return DisplayType.TABLE_COLUMN;
                case TABLE_COLUMN_GROUP:
                    return DisplayType.TABLE_COLUMN_GROUP;
                case TABLE_FOOTER_GROUP:
                    return DisplayType.TABLE_FOOTER_GROUP;
                case TABLE_HEADER_GROUP:
                    return DisplayType.TABLE_HEADER_GROUP;
                case TABLE_ROW:
                    return DisplayType.TABLE_ROW;
                case TABLE_ROW_GROUP:
                    return DisplayType.TABLE_ROW_GROUP;
                default:
                    return DisplayType.BLOCK; //this should not happen
            }
        }
        else
            return null;
    }

    @Override
    public Rectangular getSubstringBounds(int startPos, int endPos)
    {
        Box box = getBox();
        if (box instanceof TextBox)
        {
            Rectangular ret = new Rectangular(getVisualBounds());
            int origin = ret.getX1();
            int startOfs = zoom(((TextBox) box).getCharOffsetX(startPos));
            int endOfs = zoom(((TextBox) box).getCharOffsetX(endPos));
            ret.setX1(origin + startOfs);
            ret.setX2(origin + endOfs);
            return ret;
        }
        else
            return null;
    }

    private int zoom(float src)
    {
        return Math.round(src * zoom);
    }
    
}
