/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.text.chunks.ChunksSource;

/**
 * 
 * @author burgetr
 */
public class MetadataChunksExtractor extends ChunksSource
{
    private int idCounter = 1;
    private MetadataExampleGenerator exampleGenerator;
    private Map<String, List<Example>> examples;
    

    public MetadataChunksExtractor(Area root, MetadataExampleGenerator exampleGenerator)
    {
        super(root);
        this.exampleGenerator = exampleGenerator;
        examples = exampleGenerator.getMappedExamples();
    }

    @Override
    public List<TextChunk> getTextChunks()
    {
        final List<TextChunk> dest = new ArrayList<>(100);
        createChunksForSubtree(this.getRoot(), dest);
        return dest;
    }

    private boolean createChunksForSubtree(Area root, List<TextChunk> dest)
    {
        boolean childrenMatched = false;
        for (Area child : root.getChildren())
        {
            childrenMatched |= createChunksForSubtree(child, dest);
        }
        
        if (childrenMatched)
        {
            // some children matched, do not match this one anymore
            return true; 
        }
        else
        {
            // no children matched, try this node
            final String text = exampleGenerator.filterKey(getText(root));
            final List<Example> mappedExamples = examples.get(text);
            
            if (mappedExamples != null && !mappedExamples.isEmpty())
            {
                this.createChunksForArea(root, mappedExamples, dest);
                return true;
            }
            else
                return false;
        }
    }

    private void createChunksForArea(Area area, List<Example> mappedExamples, List<TextChunk> dest)
    {
        for (Example ex : mappedExamples)
            dest.add(createChunkForExample(area, ex));
    }

    private TextChunk createChunkForExample(Area area, Example example)
    {
        var rectangular = new Rectangular(area.getBounds());
        var chunk = new DefaultTextChunk();
        chunk.setId(this.idCounter++);
        chunk.setBounds(rectangular);
        chunk.setSourceArea(area);
        chunk.setText(example.getText());
        chunk.addTag(new DefaultTag("meta", example.getPredicate().getLocalName()), 0.95f);
        chunk.setName("<chunk:" + example.getPredicate().getLocalName() + "> " + example.getText());

        return chunk;
    }

    private String getText(Area area)
    {
        if (area.isLeaf())
        {
            return area.getBoxes()
                .stream()
                .map(Box::getOwnText)
                .collect(Collectors.joining());
        }
        else
        {
            return area.getChildren()
                .stream()
                .map((child) -> getText(child))
                .collect(Collectors.joining());
        }
    }
}
