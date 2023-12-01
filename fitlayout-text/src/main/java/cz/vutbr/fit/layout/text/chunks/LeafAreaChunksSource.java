/**
 * LeafAreaChunksSource.java
 *
 * Created on 23. 11. 2023, 20:32:25 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.Concatenators;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * A simple chunks source that makes a text chunk from each leaf area in the source tree.
 * 
 * @author burgetr
 */
public class LeafAreaChunksSource extends ChunksSource
{
    private int idcnt;
    private List<TextChunk> chunks;

    public LeafAreaChunksSource(Area root)
    {
        super(root);
    }
    
    @Override
    public List<TextChunk> getTextChunks()
    {
        if (chunks == null)
        {
            chunks = new ArrayList<>();
            recursiveScan(getRoot(), chunks);
        }
        return chunks;
    }
    
    private void recursiveScan(Area root, List<TextChunk> dest)
    {
        if (root.isLeaf())
        {
            TextChunk chunk = createChunkFromArea(root);
            if (chunk!= null)
                dest.add(chunk);
        }
        else
        {
            for (Area child : root.getChildren())
            {
                recursiveScan(child, dest);
            }
        }
    }

    private TextChunk createChunkFromArea(Area area)
    {
        if (area.getBoxes().size() > 0)
        {
            String text = area.getText(Concatenators.getDefaultAreaConcatenator());
            if (!text.isEmpty())
            {
                Box box = area.getBoxes().get(0); // use the first box as the source
                DefaultTextChunk newChunk = new DefaultTextChunk(area.getBounds(), area, box);
                newChunk.setId(idcnt++);
                newChunk.setText(text);
                
                // copy tags
                for (Map.Entry<Tag, Float> entry : area.getTags().entrySet())
                    newChunk.addTag(entry.getKey(), entry.getValue());
                
                return newChunk;
            }
            else
                return null;
        }
        else
            return null;
    }


}
