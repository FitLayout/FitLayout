/**
 * BoxListRenderer.java
 *
 * Created on 13. 5. 2020, 15:44:10 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fit.cssbox.awt.Transform;
import org.fit.cssbox.css.BackgroundDecoder;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.ListItemBox;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.render.StructuredRenderer;

import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;

/**
 * A CSSBox renderer that produces a list of boxes.
 * 
 * @author burgetr
 */
public class BoxListRenderer extends StructuredRenderer
{
    private Page page;
    private float zoom;
    
    /** the resulting list */
    private List<BoxNode> boxList;
    
    /** current transformation to be applied (or null) */
    private AffineTransform currentTransform;
    
    /** applied transformations */
    private Map<ElementBox, AffineTransform> savedTransforms;
    
    private int orderCounter;
    
    
    public BoxListRenderer(Page page, float zoom)
    {
        this.page = page;
        this.zoom = zoom;
        boxList = new ArrayList<>();
        savedTransforms = new HashMap<ElementBox, AffineTransform>();
        orderCounter = 0;
    }

    public List<BoxNode> getBoxList()
    {
        return boxList;
    }

    public void setBoxList(List<BoxNode> boxList)
    {
        this.boxList = boxList;
    }

    @Override
    public void startElementContents(ElementBox elem)
    {
        //setup transformations for the contents
        AffineTransform at = Transform.createTransform(elem);
        if (at != null)
        {
            savedTransforms.put(elem, currentTransform);
            currentTransform = at;
        }
    }

    @Override
    public void finishElementContents(ElementBox elem)
    {
        //restore the stransformations
        AffineTransform origAt = savedTransforms.get(elem);
        currentTransform = origAt;
    }

    @Override
    public void renderElementBackground(ElementBox elem)
    {
        // TODO apply transforms
        // background color is computed by the renderer in order to treat special Viewport behavior
        BackgroundDecoder bg = findBackgroundSource(elem);
        Color bgColor = (bg == null) ? null : Units.toColor(bg.getBgcolor());
        BoxNode newnode = new BoxNode(elem, page, bgColor, zoom);
        newnode.setOrder(orderCounter++);
        boxList.add(newnode);
    }

    @Override
    public void renderMarker(ListItemBox elem)
    {
        // TODO 
    }

    @Override
    public void renderTextContent(TextBox text)
    {
        BoxNode newnode = new BoxNode(text, page, zoom);
        newnode.setOrder(orderCounter++);
        boxList.add(newnode);
    }

    @Override
    public void renderReplacedContent(ReplacedBox box)
    {
        // the content of replaced boxes (mainly images) is not considered at the moment
    }

    @Override
    public void close() throws IOException
    {
    }

}
