package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Visual Area Ontology.
 * <p>
 * Document visual area (segmentation) ontology..
 * <p>
 * Namespace SEGM.
 * Prefix: {@code <http://fitlayout.github.io/ontology/segmentation.owl#>}
 */
public class SEGM {

	/** {@code http://fitlayout.github.io/ontology/segmentation.owl#} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/segmentation.owl#";

	/** {@code segm} **/
	public static final String PREFIX = "segm";

	/**
	 * Area
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#Area}.
	 * <p>
	 * A visual area within the page. The areas can be nested an together
	 * they form an AreaTree.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#Area">Area</a>
	 */
	public static final IRI Area;

	/**
	 * AreaTree
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#AreaTree}.
	 * <p>
	 * A tree of visual areas created from a rendered page by page
	 * segmentation.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#AreaTree">AreaTree</a>
	 */
	public static final IRI AreaTree;

	/**
	 * belongsTo
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#belongsTo}.
	 * <p>
	 * Assigns an AreaTree to an Area.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#belongsTo">belongsTo</a>
	 */
	public static final IRI belongsTo;

	/**
	 * belongsToChunkSet
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#belongsToChunkSet}.
	 * <p>
	 * Assigns a ChunkSet to a TextChunk.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#belongsToChunkSet">belongsToChunkSet</a>
	 */
	public static final IRI belongsToChunkSet;

	/**
	 * belongsToLogicalTree
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#belongsToLogicalTree}.
	 * <p>
	 * Assigns the owning LogicalAreaTree to a LogicalArea.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#belongsToLogicalTree">belongsToLogicalTree</a>
	 */
	public static final IRI belongsToLogicalTree;

	/**
	 * ChunkSet
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#ChunkSet}.
	 * <p>
	 * A set of text chunks extreacted from a source page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#ChunkSet">ChunkSet</a>
	 */
	public static final IRI ChunkSet;

	/**
	 * containsArea
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#containsArea}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#containsArea">containsArea</a>
	 */
	public static final IRI containsArea;

	/**
	 * containsBox
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#containsBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#containsBox">containsBox</a>
	 */
	public static final IRI containsBox;

	/**
	 * discriminator
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#discriminator}.
	 * <p>
	 * Adds a string discriminator to the given tag. It may be used to
	 * distinguish the tags when a single tagger assigns multiple tags.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#discriminator">discriminator</a>
	 */
	public static final IRI discriminator;

	/**
	 * hasAreaTree
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasAreaTree}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasAreaTree">hasAreaTree</a>
	 */
	public static final IRI hasAreaTree;

	/**
	 * hasRelatedRect
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasRelatedRect}.
	 * <p>
	 * Defines a related Area or TextChunk for a relation description
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasRelatedRect">hasRelatedRect</a>
	 */
	public static final IRI hasRelatedRect;

	/**
	 * hasRelationType
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasRelationType}.
	 * <p>
	 * Assigns a relation type to a relation description
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasRelationType">hasRelationType</a>
	 */
	public static final IRI hasRelationType;

	/**
	 * hasSourceArea
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourceArea}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourceArea">hasSourceArea</a>
	 */
	public static final IRI hasSourceArea;

	/**
	 * hasSourceBox
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourceBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourceBox">hasSourceBox</a>
	 */
	public static final IRI hasSourceBox;

	/**
	 * hasSourcePage
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourcePage}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourcePage">hasSourcePage</a>
	 */
	public static final IRI hasSourcePage;

	/**
	 * hasTag
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasTag}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasTag">hasTag</a>
	 */
	public static final IRI hasTag;

	/**
	 * isChildOf
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#isChildOf}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#isChildOf">isChildOf</a>
	 */
	public static final IRI isChildOf;

	/**
	 * isInRelation
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#isInRelation}.
	 * <p>
	 * Assigns an Area or TextChunk a description of a relation with another
	 * Area or TextChunk
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#isInRelation">isInRelation</a>
	 */
	public static final IRI isInRelation;

	/**
	 * isSubordinateTo
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#isSubordinateTo}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#isSubordinateTo">isSubordinateTo</a>
	 */
	public static final IRI isSubordinateTo;

	/**
	 * LogicalArea
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#LogicalArea}.
	 * <p>
	 * Logical area represents a set of areas that form a single semantic
	 * entity. Logical areas are organized in a tree where the parent-child
	 * relationships have some semantic meaning instead of representing the
	 * actual layout.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#LogicalArea">LogicalArea</a>
	 */
	public static final IRI LogicalArea;

	/**
	 * LogicalAreaTree
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#LogicalAreaTree}.
	 * <p>
	 * A tree of logical areas created from an area tree by some kind of
	 * logical structure analysis.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#LogicalAreaTree">LogicalAreaTree</a>
	 */
	public static final IRI LogicalAreaTree;

	/**
	 * name
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#name}.
	 * <p>
	 * Assigned area name
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#name">name</a>
	 */
	public static final IRI name;

	/**
	 * RelationDescription
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#RelationDescription}.
	 * <p>
	 * Describes a relation between two Areas or TextChunks
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#RelationDescription">RelationDescription</a>
	 */
	public static final IRI RelationDescription;

	/**
	 * support
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#support}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#support">support</a>
	 */
	public static final IRI support;

	/**
	 * Tag
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#Tag}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#Tag">Tag</a>
	 */
	public static final IRI Tag;

	/**
	 * tagger
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#tagger}.
	 * <p>
	 * Assigns a tagger to a tag
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#tagger">tagger</a>
	 */
	public static final IRI tagger;

	/**
	 * tagSupport
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#tagSupport}.
	 * <p>
	 * Assigns a node that refers to a tag and the assigned support.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#tagSupport">tagSupport</a>
	 */
	public static final IRI tagSupport;

	/**
	 * text
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#text}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#text">text</a>
	 */
	public static final IRI text;

	/**
	 * TextChunk
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#TextChunk}.
	 * <p>
	 * A connected piece of a document text that forms a rectangular area in
	 * the page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#TextChunk">TextChunk</a>
	 */
	public static final IRI TextChunk;

	/**
	 * type
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#type}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#type">type</a>
	 */
	public static final IRI type;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Area = factory.createIRI(SEGM.NAMESPACE, "Area");
		AreaTree = factory.createIRI(SEGM.NAMESPACE, "AreaTree");
		belongsTo = factory.createIRI(SEGM.NAMESPACE, "belongsTo");
		belongsToChunkSet = factory.createIRI(SEGM.NAMESPACE, "belongsToChunkSet");
		belongsToLogicalTree = factory.createIRI(SEGM.NAMESPACE, "belongsToLogicalTree");
		ChunkSet = factory.createIRI(SEGM.NAMESPACE, "ChunkSet");
		containsArea = factory.createIRI(SEGM.NAMESPACE, "containsArea");
		containsBox = factory.createIRI(SEGM.NAMESPACE, "containsBox");
		discriminator = factory.createIRI(SEGM.NAMESPACE, "discriminator");
		hasAreaTree = factory.createIRI(SEGM.NAMESPACE, "hasAreaTree");
		hasRelatedRect = factory.createIRI(SEGM.NAMESPACE, "hasRelatedRect");
		hasRelationType = factory.createIRI(SEGM.NAMESPACE, "hasRelationType");
		hasSourceArea = factory.createIRI(SEGM.NAMESPACE, "hasSourceArea");
		hasSourceBox = factory.createIRI(SEGM.NAMESPACE, "hasSourceBox");
		hasSourcePage = factory.createIRI(SEGM.NAMESPACE, "hasSourcePage");
		hasTag = factory.createIRI(SEGM.NAMESPACE, "hasTag");
		isChildOf = factory.createIRI(SEGM.NAMESPACE, "isChildOf");
		isInRelation = factory.createIRI(SEGM.NAMESPACE, "isInRelation");
		isSubordinateTo = factory.createIRI(SEGM.NAMESPACE, "isSubordinateTo");
		LogicalArea = factory.createIRI(SEGM.NAMESPACE, "LogicalArea");
		LogicalAreaTree = factory.createIRI(SEGM.NAMESPACE, "LogicalAreaTree");
		name = factory.createIRI(SEGM.NAMESPACE, "name");
		RelationDescription = factory.createIRI(SEGM.NAMESPACE, "RelationDescription");
		support = factory.createIRI(SEGM.NAMESPACE, "support");
		Tag = factory.createIRI(SEGM.NAMESPACE, "Tag");
		tagger = factory.createIRI(SEGM.NAMESPACE, "tagger");
		tagSupport = factory.createIRI(SEGM.NAMESPACE, "tagSupport");
		text = factory.createIRI(SEGM.NAMESPACE, "text");
		TextChunk = factory.createIRI(SEGM.NAMESPACE, "TextChunk");
		type = factory.createIRI(SEGM.NAMESPACE, "type");
	}

	private SEGM() {
		//static access only
	}

}
