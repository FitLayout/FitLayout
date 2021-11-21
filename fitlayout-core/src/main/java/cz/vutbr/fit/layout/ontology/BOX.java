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
	 * Attribute
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Attribute}.
	 * <p>
	 * An HTML attribute assigned to a box.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Attribute">Attribute</a>
	 */
	public static final IRI Attribute;

	/**
	 * backgroundColor
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#backgroundColor}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#backgroundColor">backgroundColor</a>
	 */
	public static final IRI backgroundColor;

	/**
	 * backgroundSeparated
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#backgroundSeparated}.
	 * <p>
	 * Indicates whether the rectangle is separated from it parent rectangle
	 * by background color or image.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#backgroundSeparated">backgroundSeparated</a>
	 */
	public static final IRI backgroundSeparated;

	/**
	 * belongsTo
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#belongsTo}.
	 * <p>
	 * Assigns an owning page to a rectangle
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#belongsTo">belongsTo</a>
	 */
	public static final IRI belongsTo;

	/**
	 * Border
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Border}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Border">Border</a>
	 */
	public static final IRI Border;

	/**
	 * borderColor
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderColor}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderColor">borderColor</a>
	 */
	public static final IRI borderColor;

	/**
	 * borderStyle
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderStyle}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderStyle">borderStyle</a>
	 */
	public static final IRI borderStyle;

	/**
	 * borderWidth
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#borderWidth}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#borderWidth">borderWidth</a>
	 */
	public static final IRI borderWidth;

	/**
	 * Bounds
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Bounds}.
	 * <p>
	 * Rectangular bounds specified by its coordinates, width and height.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Bounds">Bounds</a>
	 */
	public static final IRI Bounds;

	/**
	 * bounds
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#bounds}.
	 * <p>
	 * Assigns logical rectangular bounds to an area.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#bounds">bounds</a>
	 */
	public static final IRI bounds;

	/**
	 * Box
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Box}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Box">Box</a>
	 */
	public static final IRI Box;

	/**
	 * color
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#color}.
	 * <p>
	 * Foreground color (#rrggbb)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#color">color</a>
	 */
	public static final IRI color;

	/**
	 * ContainerBox
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContainerBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContainerBox">ContainerBox</a>
	 */
	public static final IRI ContainerBox;

	/**
	 * containsObject
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#containsObject}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#containsObject">containsObject</a>
	 */
	public static final IRI containsObject;

	/**
	 * contentBounds
	 * <p>
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
	 * ContentBox
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContentBox}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContentBox">ContentBox</a>
	 */
	public static final IRI ContentBox;

	/**
	 * contentLength
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#contentLength}.
	 * <p>
	 * The number of content elements used to compute the style statistics
	 * such as average font weight.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#contentLength">contentLength</a>
	 */
	public static final IRI contentLength;

	/**
	 * ContentObject
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#ContentObject}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#ContentObject">ContentObject</a>
	 */
	public static final IRI ContentObject;

	/**
	 * displayType
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#displayType}.
	 * <p>
	 * The display type of a box that corresponds to the CSS 'display'
	 * property ('inline', 'block', etc.)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#displayType">displayType</a>
	 */
	public static final IRI displayType;

	/**
	 * documentOrder
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#documentOrder}.
	 * <p>
	 * The order of a rectangle within its page
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#documentOrder">documentOrder</a>
	 */
	public static final IRI documentOrder;

	/**
	 * fontFamily
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontFamily}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontFamily">fontFamily</a>
	 */
	public static final IRI fontFamily;

	/**
	 * fontSize
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontSize}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontSize">fontSize</a>
	 */
	public static final IRI fontSize;

	/**
	 * fontStyle
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontStyle}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontStyle">fontStyle</a>
	 */
	public static final IRI fontStyle;

	/**
	 * fontVariant
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontVariant}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontVariant">fontVariant</a>
	 */
	public static final IRI fontVariant;

	/**
	 * fontWeight
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#fontWeight}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#fontWeight">fontWeight</a>
	 */
	public static final IRI fontWeight;

	/**
	 * hasAttribute
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasAttribute}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasAttribute">hasAttribute</a>
	 */
	public static final IRI hasAttribute;

	/**
	 * hasBackgroundImage
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasBackgroundImage}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasBackgroundImage">hasBackgroundImage</a>
	 */
	public static final IRI hasBackgroundImage;

	/**
	 * hasBottomBorder
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasBottomBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasBottomBorder">hasBottomBorder</a>
	 */
	public static final IRI hasBottomBorder;

	/**
	 * hasLeftBorder
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasLeftBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasLeftBorder">hasLeftBorder</a>
	 */
	public static final IRI hasLeftBorder;

	/**
	 * hasRightBorder
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasRightBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasRightBorder">hasRightBorder</a>
	 */
	public static final IRI hasRightBorder;

	/**
	 * hasTopBorder
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#hasTopBorder}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#hasTopBorder">hasTopBorder</a>
	 */
	public static final IRI hasTopBorder;

	/**
	 * height
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#height}.
	 * <p>
	 * Effective height of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#height">height</a>
	 */
	public static final IRI height;

	/**
	 * htmlTagName
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#htmlTagName}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#htmlTagName">htmlTagName</a>
	 */
	public static final IRI htmlTagName;

	/**
	 * Image
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Image}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Image">Image</a>
	 */
	public static final IRI Image;

	/**
	 * imageData
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#imageData}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#imageData">imageData</a>
	 */
	public static final IRI imageData;

	/**
	 * imageUrl
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#imageUrl}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#imageUrl">imageUrl</a>
	 */
	public static final IRI imageUrl;

	/**
	 * isChildOf
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#isChildOf}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#isChildOf">isChildOf</a>
	 */
	public static final IRI isChildOf;

	/**
	 * lineThrough
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#lineThrough}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#lineThrough">lineThrough</a>
	 */
	public static final IRI lineThrough;

	/**
	 * Page
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#Page}.
	 * <p>
	 * A tree of boxes representing a rendered page.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#Page">Page</a>
	 */
	public static final IRI Page;

	/**
	 * pngImage
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#pngImage}.
	 * <p>
	 * PNG image data representing the whole page (screen shot)
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#pngImage">pngImage</a>
	 */
	public static final IRI pngImage;

	/**
	 * positionX
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#positionX}.
	 * <p>
	 * Effective X coordinate of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#positionX">positionX</a>
	 */
	public static final IRI positionX;

	/**
	 * positionY
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#positionY}.
	 * <p>
	 * Effective Y coordinate of a rectangle.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#positionY">positionY</a>
	 */
	public static final IRI positionY;

	/**
	 * RectArea
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#RectArea}.
	 * <p>
	 * A rectangular area in the page with bounds assigned.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#RectArea">RectArea</a>
	 */
	public static final IRI RectArea;

	/**
	 * sourceUrl
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#sourceUrl}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#sourceUrl">sourceUrl</a>
	 */
	public static final IRI sourceUrl;

	/**
	 * sourceXPath
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#sourceXPath}.
	 * <p>
	 * An XPath expression identifying the source element of the box in the
	 * source document.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#sourceXPath">sourceXPath</a>
	 */
	public static final IRI sourceXPath;

	/**
	 * text
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#text}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#text">text</a>
	 */
	public static final IRI text;

	/**
	 * title
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#title}.
	 * <p>
	 * Page title
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#title">title</a>
	 */
	public static final IRI title;

	/**
	 * underline
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#underline}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#underline">underline</a>
	 */
	public static final IRI underline;

	/**
	 * visible
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/render.owl#visible}.
	 * <p>
	 * Defines the box visibility
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/render.owl#visible">visible</a>
	 */
	public static final IRI visible;

	/**
	 * visualBounds
	 * <p>
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
	 * width
	 * <p>
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
		backgroundSeparated = factory.createIRI(BOX.NAMESPACE, "backgroundSeparated");
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
		contentLength = factory.createIRI(BOX.NAMESPACE, "contentLength");
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
