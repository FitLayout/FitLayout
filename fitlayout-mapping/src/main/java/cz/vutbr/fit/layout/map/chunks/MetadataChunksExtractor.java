/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.ExampleMatcher;
import cz.vutbr.fit.layout.map.MetaRefTag;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.map.TextUtils;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.text.chunks.ChunksSource;

/**
 * 
 * @author burgetr
 */
public class MetadataChunksExtractor extends ChunksSource
{
    private static Logger log = LoggerFactory.getLogger(MetadataChunksExtractor.class);
    
    private int idCounter = 1;
    private Map<Example, MetaRefTag> assignedTags;
    private ExampleMatcher matcher;
    

    public MetadataChunksExtractor(Area root, MetadataExampleGenerator exampleGenerator, Collection<MetaRefTag> tags)
    {
        super(root);
        matcher = new ExampleMatcher(exampleGenerator);
        
        //create index of tags
        assignedTags = new HashMap<>();
        for (var tag : tags)
            assignedTags.put(tag.getExample(), tag);
    }

    public Collection<MetaRefTag> getAssignedTags()
    {
        return assignedTags.values();
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
            return matcher.match(TextUtils.getText(root),
                    (mappedExamples) -> this.createChunksForArea(root, mappedExamples, dest));
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
        MetaRefTag tag = assignedTags.get(example);
        if (tag == null)
            log.warn("No tag found for example {}", example);
        else
            chunk.addTag(tag, 0.95f);
        chunk.setName("<chunk:" + example.getPredicate().getLocalName() + "> " + example.getText());

        return chunk;
    }
    
}
