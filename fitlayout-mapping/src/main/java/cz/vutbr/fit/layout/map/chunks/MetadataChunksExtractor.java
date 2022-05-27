/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return this.createChunksForSubtree(this.getRoot())
                .collect(Collectors.toList());
    }

    private Stream<TextChunk> createChunksForSubtree(Area root)
    {
        if (root.toString().contains("nameextc"))
            System.out.println("jo!");
        final String text = exampleGenerator.filterKey(getText(root));
        final List<Example> mappedExamples = examples.get(text);
        
        if (mappedExamples != null && !mappedExamples.isEmpty())
            return this.createChunksForArea(root, mappedExamples);

        return root.getChildren()
            .stream()
            .flatMap(this::createChunksForSubtree);
    }

    private Stream<TextChunk> createChunksForArea(Area area, List<Example> mappedExamples)
    {
        return mappedExamples.stream()
            .map(example -> this.createChunkForExample(area, example));
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
