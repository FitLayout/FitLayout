/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetaRefTag;
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
    private Map<Float, List<Example>> floatExamples;
    private Set<MetaRefTag> assignedTags;
    

    public MetadataChunksExtractor(Area root, MetadataExampleGenerator exampleGenerator)
    {
        super(root);
        this.exampleGenerator = exampleGenerator;
        examples = exampleGenerator.getStringExamples();
        floatExamples = exampleGenerator.getFloatExamples();
        assignedTags = new HashSet<>();
    }

    public Set<MetaRefTag> getAssignedTags()
    {
        return assignedTags;
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
            Set<Example> usedExamples = new HashSet<>();
            boolean ret = tryStringExamples(root, dest, usedExamples);
            if (!ret && !floatExamples.isEmpty())
                ret |= tryFloatExamples(root, dest, usedExamples);
            return ret;
        }
    }

    private boolean tryStringExamples(Area root, List<TextChunk> dest, Set<Example> usedExamples)
    {
        final String text = exampleGenerator.filterKey(getText(root));
        final List<Example> mappedExamples = examples.get(text);
        
        if (mappedExamples != null && !mappedExamples.isEmpty())
        {
            this.createChunksForArea(root, mappedExamples, dest);
            usedExamples.addAll(mappedExamples);
            return true;
        }
        else
            return false;
    }
    
    private boolean tryFloatExamples(Area root, List<TextChunk> dest, Set<Example> usedExamples)
    {
        String text = getText(root).replaceAll("[^a-zA-Z0-9\\,\\.]", "");
        if (text.contains(",") && !text.contains(".")) // may be comma used for decimals
            text = text.replace(',', '.');
        try {
            float val = Float.parseFloat(text);
            final List<Example> mappedExamples = floatExamples.get(val);
            if (mappedExamples != null)
            {
                mappedExamples.removeAll(usedExamples); //avoid re-using examples already mapped as strings
                if (!mappedExamples.isEmpty())
                {
                    this.createChunksForArea(root, mappedExamples, dest);
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
        } catch (NumberFormatException e) {
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
        final var tag = new MetaRefTag(example.getPredicate().getLocalName(), example);
        assignedTags.add(tag);
        chunk.addTag(tag, 0.95f);
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
