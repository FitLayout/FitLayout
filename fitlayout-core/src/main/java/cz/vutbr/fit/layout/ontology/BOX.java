package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Box Model Ontology.
 * <p>
 * FitLayout rendered document (box model) description ontology..
 * <p>
 * Namespace BOX.
 * Prefix: {@code <http://fitlayout.github.io/ontology/render.owl#>}
 */
public class BOX {

	/** {@code http://fitlayout.github.io/ontology/render.owl#} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/render.owl#";

	/** {@code box} **/
	public static final String PREFIX = "box";

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Attribute}.
	 * <p>
	 * An HTML attribute assigned to a box.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Attribute">Attribute</a>
	 */
	public static final IRI Attribute;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#backgroundColor}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#backgroundColor">backgroundColor</a>
	 */
	public static final IRI backgroundColor;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#belongsTo}.
	 * <p>
	 * Assigns an owning page to a rectangle
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#belongsTo">belongsTo</a>
	 */
	public static final IRI belongsTo;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Border}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Border">Border</a>
	 */
	public static final IRI Border;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderColor}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderColor">borderColor</a>
	 */
	public static final IRI borderColor;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderStyle}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderStyle">borderStyle</a>
	 */
	public static final IRI borderStyle;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderWidth}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderWidth">borderWidth</a>
	 */
	public static final IRI borderWidth;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Bounds}.
	 * <p>
	 * Rectangular bounds specified by its coordinates, width and height.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Bounds">Bounds</a>
	 */
	public static final IRI Bounds;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#bounds}.
	 * <p>
	 * Assigns logical rectangular bounds to an area.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#bounds">bounds</a>
	 */
	public static final IRI bounds;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Box}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Box">Box</a>
	 */
	public static final IRI Box;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#color}.
	 * <p>
	 * Foreground color (#rrggbb)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#color">color</a>
	 */
	public static final IRI color;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContainerBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContainerBox">ContainerBox</a>
	 */
	public static final IRI ContainerBox;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#containsObject}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#containsObject">containsObject</a>
	 */
	public static final IRI containsObject;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#contentBounds}.
	 * <p>
	 * Assigns rectangular content bounds to a box. The content bounds
	 * correspond to the box border bounds as provided by the box source
	 * (renderer).
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#contentBounds">contentBounds</a>
	 */
	public static final IRI contentBounds;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContentBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContentBox">ContentBox</a>
	 */
	public static final IRI ContentBox;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContentObject}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContentObject">ContentObject</a>
	 */
	public static final IRI ContentObject;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#displayType}.
	 * <p>
	 * The display type of a box that corresponds to the CSS 'display'
	 * property ('inline', 'block', etc.)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#displayType">displayType</a>
	 */
	public static final IRI displayType;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#documentOrder}.
	 * <p>
	 * The order of a rectangle within its page
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#documentOrder">documentOrder</a>
	 */
	public static final IRI documentOrder;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontFamily}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontFamily">fontFamily</a>
	 */
	public static final IRI fontFamily;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontSize}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontSize">fontSize</a>
	 */
	public static final IRI fontSize;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontStyle}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontStyle">fontStyle</a>
	 */
	public static final IRI fontStyle;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontVariant}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontVariant">fontVariant</a>
	 */
	public static final IRI fontVariant;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontWeight}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontWeight">fontWeight</a>
	 */
	public static final IRI fontWeight;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasAttribute}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasAttribute">hasAttribute</a>
	 */
	public static final IRI hasAttribute;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasBackgroundImage}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasBackgroundImage">hasBackgroundImage</a>
	 */
	public static final IRI hasBackgroundImage;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasBottomBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasBottomBorder">hasBottomBorder</a>
	 */
	public static final IRI hasBottomBorder;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasLeftBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasLeftBorder">hasLeftBorder</a>
	 */
	public static final IRI hasLeftBorder;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasRightBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasRightBorder">hasRightBorder</a>
	 */
	public static final IRI hasRightBorder;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasTopBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasTopBorder">hasTopBorder</a>
	 */
	public static final IRI hasTopBorder;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#height}.
	 * <p>
	 * Effective height of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#height">height</a>
	 */
	public static final IRI height;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#htmlTagName}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#htmlTagName">htmlTagName</a>
	 */
	public static final IRI htmlTagName;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Image}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Image">Image</a>
	 */
	public static final IRI Image;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#imageData}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#imageData">imageData</a>
	 */
	public static final IRI imageData;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#imageUrl}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#imageUrl">imageUrl</a>
	 */
	public static final IRI imageUrl;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#isChildOf}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#isChildOf">isChildOf</a>
	 */
	public static final IRI isChildOf;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#lineThrough}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#lineThrough">lineThrough</a>
	 */
	public static final IRI lineThrough;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#Page}.
	 * <p>
	 * A tree of boxes representing a rendered page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Page">Page</a>
	 */
	public static final IRI Page;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#pngImage}.
	 * <p>
	 * PNG image data representing the whole page (screen shot)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#pngImage">pngImage</a>
	 */
	public static final IRI pngImage;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#positionX}.
	 * <p>
	 * Effective X coordinate of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#positionX">positionX</a>
	 */
	public static final IRI positionX;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#positionY}.
	 * <p>
	 * Effective Y coordinate of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#positionY">positionY</a>
	 */
	public static final IRI positionY;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#RectArea}.
	 * <p>
	 * A rectangular area in the page with bounds assigned.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#RectArea">RectArea</a>
	 */
	public static final IRI RectArea;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#sourceUrl}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#sourceUrl">sourceUrl</a>
	 */
	public static final IRI sourceUrl;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#sourceXPath}.
	 * <p>
	 * An XPath expression identifying the source element of the box in the
	 * source document.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#sourceXPath">sourceXPath</a>
	 */
	public static final IRI sourceXPath;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#text}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#text">text</a>
	 */
	public static final IRI text;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#title}.
	 * <p>
	 * Page title
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#title">title</a>
	 */
	public static final IRI title;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#underline}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#underline">underline</a>
	 */
	public static final IRI underline;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#visible}.
	 * <p>
	 * Defines the box visibility
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#visible">visible</a>
	 */
	public static final IRI visible;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#visualBounds}.
	 * <p>
	 * Assigns visual rectangular bounds to a box. Visual bounds correspond
	 * to the minimal rectangle that encloses visible contents inside the
	 * box.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#visualBounds">visualBounds</a>
	 */
	public static final IRI visualBounds;

	/**
	 * {@code http://fitlayout.github.io/ontology/render.owl#width}.
	 * <p>
	 * Effective width of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#width">width</a>
	 */
	public static final IRI width;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Attribute = factory.createIRI(BOX.NAMESPACE, "Attribute");
		backgroundColor = factory.createIRI(BOX.NAMESPACE, "backgroundColor");
		belongsTo = factory.createIRI(BOX.NAMESPACE, "belongsTo");
		Border = factory.createIRI(BOX.NAMESPACE, "Border");
		borderColor = factory.createIRI(BOX.NAMESPACE, "borderColor");
		borderStyle = factory.createIRI(BOX.NAMESPACE, "borderStyle");
		borderWidth = factory.createIRI(BOX.NAMESPACE, "borderWidth");
		Bounds = factory.createIRI(BOX.NAMESPACE, "Bounds");
		bounds = factory.createIRI(BOX.NAMESPACE, "bounds");
		Box = factory.createIRI(BOX.NAMESPACE, "Box");
		color = factory.createIRI(BOX.NAMESPACE, "color");
		ContainerBox = factory.createIRI(BOX.NAMESPACE, "ContainerBox");
		containsObject = factory.createIRI(BOX.NAMESPACE, "containsObject");
		contentBounds = factory.createIRI(BOX.NAMESPACE, "contentBounds");
		ContentBox = factory.createIRI(BOX.NAMESPACE, "ContentBox");
		ContentObject = factory.createIRI(BOX.NAMESPACE, "ContentObject");
		displayType = factory.createIRI(BOX.NAMESPACE, "displayType");
		documentOrder = factory.createIRI(BOX.NAMESPACE, "documentOrder");
		fontFamily = factory.createIRI(BOX.NAMESPACE, "fontFamily");
		fontSize = factory.createIRI(BOX.NAMESPACE, "fontSize");
		fontStyle = factory.createIRI(BOX.NAMESPACE, "fontStyle");
		fontVariant = factory.createIRI(BOX.NAMESPACE, "fontVariant");
		fontWeight = factory.createIRI(BOX.NAMESPACE, "fontWeight");
		hasAttribute = factory.createIRI(BOX.NAMESPACE, "hasAttribute");
		hasBackgroundImage = factory.createIRI(BOX.NAMESPACE, "hasBackgroundImage");
		hasBottomBorder = factory.createIRI(BOX.NAMESPACE, "hasBottomBorder");
		hasLeftBorder = factory.createIRI(BOX.NAMESPACE, "hasLeftBorder");
		hasRightBorder = factory.createIRI(BOX.NAMESPACE, "hasRightBorder");
		hasTopBorder = factory.createIRI(BOX.NAMESPACE, "hasTopBorder");
		height = factory.createIRI(BOX.NAMESPACE, "height");
		htmlTagName = factory.createIRI(BOX.NAMESPACE, "htmlTagName");
		Image = factory.createIRI(BOX.NAMESPACE, "Image");
		imageData = factory.createIRI(BOX.NAMESPACE, "imageData");
		imageUrl = factory.createIRI(BOX.NAMESPACE, "imageUrl");
		isChildOf = factory.createIRI(BOX.NAMESPACE, "isChildOf");
		lineThrough = factory.createIRI(BOX.NAMESPACE, "lineThrough");
		Page = factory.createIRI(BOX.NAMESPACE, "Page");
		pngImage = factory.createIRI(BOX.NAMESPACE, "pngImage");
		positionX = factory.createIRI(BOX.NAMESPACE, "positionX");
		positionY = factory.createIRI(BOX.NAMESPACE, "positionY");
		RectArea = factory.createIRI(BOX.NAMESPACE, "RectArea");
		sourceUrl = factory.createIRI(BOX.NAMESPACE, "sourceUrl");
		sourceXPath = factory.createIRI(BOX.NAMESPACE, "sourceXPath");
		text = factory.createIRI(BOX.NAMESPACE, "text");
		title = factory.createIRI(BOX.NAMESPACE, "title");
		underline = factory.createIRI(BOX.NAMESPACE, "underline");
		visible = factory.createIRI(BOX.NAMESPACE, "visible");
		visualBounds = factory.createIRI(BOX.NAMESPACE, "visualBounds");
		width = factory.createIRI(BOX.NAMESPACE, "width");
	}

	private BOX() {
		//static access only
	}

}
