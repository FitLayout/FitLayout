package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Namespace SEGM.
 * Prefix: {@code <http://fitlayout.github.io/ontology/segmentation.owl#>}
 */
public class SEGM {

	/** {@code http://fitlayout.github.io/ontology/segmentation.owl#} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/segmentation.owl#";

	/** {@code segm} **/
	public static final String PREFIX = "segm";

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#Area}.
	 * <p>
	 * A visual area within the page. The areas can be nested an together
	 * they form an AreaTree.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#Area">Area</a>
	 */
	public static final IRI Area;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#AreaTree}.
	 * <p>
	 * A tree of visual areas created from a rendered page by page
	 * segmentation.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#AreaTree">AreaTree</a>
	 */
	public static final IRI AreaTree;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#belongsTo}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#belongsTo">belongsTo</a>
	 */
	public static final IRI belongsTo;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#belongsToChunkSet}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#belongsToChunkSet">belongsToChunkSet</a>
	 */
	public static final IRI belongsToChunkSet;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#ChunkSet}.
	 * <p>
	 * A set of text chunks extreacted from a source page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#ChunkSet">ChunkSet</a>
	 */
	public static final IRI ChunkSet;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#containsArea}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#containsArea">containsArea</a>
	 */
	public static final IRI containsArea;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#containsBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#containsBox">containsBox</a>
	 */
	public static final IRI containsBox;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasAreaTree}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasAreaTree">hasAreaTree</a>
	 */
	public static final IRI hasAreaTree;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasContentLength}.
	 * <p>
	 * The number of content elements used to compute the style statistics
	 * such as average font weight.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasContentLength">hasContentLength</a>
	 */
	public static final IRI hasContentLength;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasName}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasName">hasName</a>
	 */
	public static final IRI hasName;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourceArea}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourceArea">hasSourceArea</a>
	 */
	public static final IRI hasSourceArea;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourceBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourceBox">hasSourceBox</a>
	 */
	public static final IRI hasSourceBox;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasSourcePage}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasSourcePage">hasSourcePage</a>
	 */
	public static final IRI hasSourcePage;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasTag}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasTag">hasTag</a>
	 */
	public static final IRI hasTag;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasText}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasText">hasText</a>
	 */
	public static final IRI hasText;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#hasType}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#hasType">hasType</a>
	 */
	public static final IRI hasType;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#isChildOf}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#isChildOf">isChildOf</a>
	 */
	public static final IRI isChildOf;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#isSubordinateTo}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#isSubordinateTo">isSubordinateTo</a>
	 */
	public static final IRI isSubordinateTo;

	/**
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
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#LogicalAreaTree}.
	 * <p>
	 * A tree of logical areas created from an area tree by some kind of
	 * logical structure analysis.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#LogicalAreaTree">LogicalAreaTree</a>
	 */
	public static final IRI LogicalAreaTree;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#support}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#support">support</a>
	 */
	public static final IRI support;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#Tag}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#Tag">Tag</a>
	 */
	public static final IRI Tag;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#tagSupport}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#tagSupport">tagSupport</a>
	 */
	public static final IRI tagSupport;

	/**
	 * {@code http://fitlayout.github.io/ontology/segmentation.owl#TextChunk}.
	 * <p>
	 * A connected piece of a document text that forms a rectangular area in
	 * the page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/segmentation.owl#TextChunk">TextChunk</a>
	 */
	public static final IRI TextChunk;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Area = factory.createIRI(SEGM.NAMESPACE, "Area");
		AreaTree = factory.createIRI(SEGM.NAMESPACE, "AreaTree");
		belongsTo = factory.createIRI(SEGM.NAMESPACE, "belongsTo");
		belongsToChunkSet = factory.createIRI(SEGM.NAMESPACE, "belongsToChunkSet");
		ChunkSet = factory.createIRI(SEGM.NAMESPACE, "ChunkSet");
		containsArea = factory.createIRI(SEGM.NAMESPACE, "containsArea");
		containsBox = factory.createIRI(SEGM.NAMESPACE, "containsBox");
		hasAreaTree = factory.createIRI(SEGM.NAMESPACE, "hasAreaTree");
		hasContentLength = factory.createIRI(SEGM.NAMESPACE, "hasContentLength");
		hasName = factory.createIRI(SEGM.NAMESPACE, "hasName");
		hasSourceArea = factory.createIRI(SEGM.NAMESPACE, "hasSourceArea");
		hasSourceBox = factory.createIRI(SEGM.NAMESPACE, "hasSourceBox");
		hasSourcePage = factory.createIRI(SEGM.NAMESPACE, "hasSourcePage");
		hasTag = factory.createIRI(SEGM.NAMESPACE, "hasTag");
		hasText = factory.createIRI(SEGM.NAMESPACE, "hasText");
		hasType = factory.createIRI(SEGM.NAMESPACE, "hasType");
		isChildOf = factory.createIRI(SEGM.NAMESPACE, "isChildOf");
		isSubordinateTo = factory.createIRI(SEGM.NAMESPACE, "isSubordinateTo");
		LogicalArea = factory.createIRI(SEGM.NAMESPACE, "LogicalArea");
		LogicalAreaTree = factory.createIRI(SEGM.NAMESPACE, "LogicalAreaTree");
		support = factory.createIRI(SEGM.NAMESPACE, "support");
		Tag = factory.createIRI(SEGM.NAMESPACE, "Tag");
		tagSupport = factory.createIRI(SEGM.NAMESPACE, "tagSupport");
		TextChunk = factory.createIRI(SEGM.NAMESPACE, "TextChunk");
	}

	private SEGM() {
		//static access only
	}

}
