package cz.vutbr.fit.layout.text.chunks;

import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.model.TextChunk;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A text chunks source that extracts the tagged chunks only.
 * 
 * @author Martin Kotras
 * @author burgetr
 */
public class AreaTextChunksSource extends ChunksSource
{
    private static Logger log = LoggerFactory.getLogger(AreaTextChunksSource.class);
    
	private int idCounter = 1;

	private final float minSupport = 0.1f;
	private final TaggerConfig taggerConfig;

	public AreaTextChunksSource(Area root, TaggerConfig taggerConfig)
	{
		super(root);
		this.taggerConfig = taggerConfig;
	}

	@Override
	public List<TextChunk> getTextChunks()
	{
		return this.createChunksForArea(this.getRoot())
		        .collect(Collectors.toList());
	}

	private Stream<TextChunk> createChunksForArea(Area area)
	{
		if (area.isLeaf())
		{
		    // leaf areas - extract the chunks
			return this.createChunksForLeaf(area);
		}
		else if (area.getChildCount() > 1 && !area.getTags().isEmpty())
		{
		    // tagged non-leaf areas with multiple children
		    // e.g. a field consisting of multiple lines
		    // try to extract the chunks and continue to subareas
		    var ret1 = this.createChunksForLeaf(area);
		    var ret2 = area.getChildren()
	                .stream()
	                .flatMap(this::createChunksForArea);
		    return Stream.concat(ret1, ret2);
		}
		else
		{
		    // just continue to subareas
    		return area.getChildren()
    			.stream()
    			.flatMap(this::createChunksForArea);
		}
	}

	private Stream<TextChunk> createChunksForLeaf(Area area)
	{
		return area.getSupportedTags(this.minSupport)
			.stream()
			.flatMap(tag -> this.createChunksForTag(area, tag));
	}

	private Stream<TextChunk> createChunksForTag(Area area, Tag tag)
	{
		final var tagger = this.taggerConfig.getTaggerForTag(tag);
		if (tagger != null)
		{
    		final var text = this.getText(area);
    		return tagger.extract(text)
    			.stream()
    			.map(tagOccurrence -> this.createChunk(area, tag, tagOccurrence));
		}
		else
		{
		    log.warn("Couldn't find tagger for {}", tag);
		    return Stream.empty();
		}
	}

	private TextChunk createChunk(Area area, Tag tag, TagOccurrence tagOccurrence)
	{
		var rectangular = new Rectangular(area.getBounds());
		var chunk = new DefaultTextChunk();
		chunk.setId(this.idCounter++);
		chunk.setBounds(rectangular);
		chunk.setSourceArea(area);
		chunk.setText(tagOccurrence.getText());
		chunk.addTag(tag, area.getTagSupport(tag));
        chunk.setName("<chunk:" + tag.getName() + "> " + tagOccurrence.getText());

		return chunk;
	}

	private String getText(Area area)
	{
		return area.getAllBoxes()
			.stream()
			.map(Box::getOwnText)
			.collect(Collectors.joining());
	}
}
