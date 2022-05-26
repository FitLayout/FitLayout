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

/**
 * A text chunks source that extracts the tagged chunks only.
 * 
 * @author Martin Kotras
 */
public class AreaTextChunksSource extends ChunksSource
{
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
			return this.createChunksForLeaf(area);

		return area.getChildren()
			.stream()
			.flatMap(this::createChunksForArea);
	}

	private Stream<TextChunk> createChunksForLeaf(Area area)
	{
		return area.getSupportedTags(this.minSupport)
			.stream()
			.flatMap(tag -> this.createChunksForTag(area, tag));
	}

	private Stream<TextChunk> createChunksForTag(Area area, Tag tag)
	{
		var tagger = this.taggerConfig.getTaggerForTag(tag);
		var text = this.getText(area);

		return tagger.extract(text)
			.stream()
			.map(tagOccurrence -> this.createChunk(area, tag, tagOccurrence));
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
		return area.getBoxes()
			.stream()
			.map(Box::getOwnText)
			.collect(Collectors.joining());
	}
}
