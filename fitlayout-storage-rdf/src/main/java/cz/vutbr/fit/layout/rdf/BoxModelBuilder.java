package cz.vutbr.fit.layout.rdf;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
//import org.eclipse.rdf4j.query.algebra.evaluation.function.rdfterm.UUID;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.ContentImage;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * Implements an RDF graph construction from a page box model. 
 * 
 * @author milicka
 * @author burgetr 
 */
public class BoxModelBuilder extends ModelBuilderBase implements ModelBuilder
{
	private ValueFactory vf;
	
	public BoxModelBuilder(IRIFactory iriFactory) 
	{
        super(iriFactory);
        vf = SimpleValueFactory.getInstance();
	}
	
	@Override
	public Model createGraph(Artifact artifact)
	{
	    return createPageGraph((Page) artifact, artifact.getIri());
	}
	
	private Model createPageGraph(Page page, IRI pageNode) 
	{
        String baseUrl = page.getSourceURL().toString();
	    
	    Model graph = new LinkedHashModel(); // it holds whole model
		
		// store basic page data
		addArtifactData(graph, page);
		graph.add(pageNode, BOX.width, vf.createLiteral(page.getWidth()));
        graph.add(pageNode, BOX.height, vf.createLiteral(page.getHeight()));
		graph.add(pageNode, BOX.sourceUrl, vf.createLiteral(baseUrl));
		if (page.getTitle() != null)
		    graph.add(pageNode, BOX.hasTitle, vf.createLiteral(page.getTitle()));
		if (page.getPngImage() != null)
		    graph.add(pageNode, BOX.pngImage, vf.createLiteral(Base64.getEncoder().encodeToString(page.getPngImage())));

        // recursively add the boxes
		Box root = page.getRoot();
        insertBox(root, pageNode, graph);
        insertChildBoxes(root, pageNode, graph);
		
		return graph;
	}

	/**
	 * Recursively inserts the child boxes of a root box into the grapgh.
	 * @param root the root box of the subtree to be inserted
	 */
	private void insertChildBoxes(Box root, IRI pageNode, Model graph) 
	{
		for (int i = 0; i < root.getChildCount(); i++)
		{
		    final Box child = root.getChildAt(i); 
			insertBox(child, pageNode, graph);
			insertChildBoxes(child, pageNode, graph);
		}
	}

	/**
	 * Appends a single box into graph model.
	 * @param box
	 */
	private void insertBox(Box box, IRI pageNode, Model graph) 
	{
		// add BOX individual into graph
		final IRI individual = getIriFactory().createBoxURI(pageNode, box);
		graph.add(individual, RDF.TYPE, BOX.Box);
		graph.add(individual, BOX.documentOrder, vf.createLiteral(box.getOrder()));
        graph.add(individual, BOX.visible, vf.createLiteral(box.isVisible()));

		// pin to page node
		graph.add(individual, BOX.belongsTo, pageNode);
		
		//parent
		if (box.getParent() != null)
		    graph.add(individual, BOX.isChildOf, getIriFactory().createBoxURI(pageNode, box.getParent()));

		//tag properties
		if (box.getTagName() != null)
		    graph.add(individual, BOX.htmlTagName, vf.createLiteral(box.getTagName()));
		
		//display type
		if (box.getDisplayType() != null)
		    graph.add(individual, BOX.displayType, vf.createLiteral(Serialization.displayTypeString(box.getDisplayType())));
		
		//attributes
		Map<String, String> attrs = box.getAttributes();
		for (Map.Entry<String, String> attr : attrs.entrySet())
		{
		    IRI attrUri = insertAttribute(individual, attr.getKey(), attr.getValue(), graph);
		    graph.add(individual, BOX.hasAttribute, attrUri);
		}
		
		// store the positions and sizes of the element
		insertBounds(individual, BOX.bounds, "b", box.getBounds(), graph);
        insertBounds(individual, BOX.visualBounds, "v", box.getVisualBounds(), graph);
        insertBounds(individual, BOX.contentBounds, "c", box.getContentBounds(), graph);
		
		if (box.getBackgroundColor() != null)
		{
            graph.add(individual, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(box.getBackgroundColor())));
		}
		if (box.getBackgroundImagePng() != null)
		{
		    graph.add(individual, BOX.backgroundImageData, vf.createLiteral(Base64.getEncoder().encodeToString(box.getBackgroundImagePng())));
		}

		// add text content into element
		if (box.getType() == Type.TEXT_CONTENT) 
		{
			graph.add(individual, BOX.hasText, vf.createLiteral(box.getText()));
		}
		else if (box.getType() == Type.REPLACED_CONTENT)
		{
		    ContentObject obj = box.getContentObject();
            //IRI objuri = null;//(new UUID()).evaluate(vf); //TODO
		    UUID uuid = UUID.randomUUID(); //TODO check duplicates
            IRI objuri = vf.createIRI("urn:uuid:" + uuid.toString());
            if (obj instanceof ContentImage)
            {
                graph.add(objuri, RDF.TYPE, BOX.Image);
                java.net.URL url = ((ContentImage) obj).getUrl();
                if (url != null)
                    graph.add(objuri, BOX.imageUrl, vf.createLiteral(url.toString()));
                byte[] imageData = ((ContentImage) obj).getPngData();
                if (imageData != null)
                    graph.add(objuri, BOX.imageData, vf.createLiteral(Base64.getEncoder().encodeToString(imageData)));
                graph.add(individual, BOX.containsImage, objuri);
            }
            else
            {
                graph.add(objuri, RDF.TYPE, BOX.ContentObject);
                graph.add(individual, BOX.containsObject, objuri);
            }
		}
		// font attributes
		graph.add(individual, BOX.fontFamily, vf.createLiteral(box.getFontFamily()));
		graph.add(individual, BOX.fontSize, vf.createLiteral(box.getTextStyle().getFontSize()));
		graph.add(individual, BOX.fontWeight, vf.createLiteral(box.getTextStyle().getFontWeight()));
		graph.add(individual, BOX.fontStyle, vf.createLiteral(box.getTextStyle().getFontStyle()));
        graph.add(individual, BOX.underline, vf.createLiteral(box.getTextStyle().getUnderline()));
        graph.add(individual, BOX.lineThrough, vf.createLiteral(box.getTextStyle().getLineThrough()));
        graph.add(individual, BOX.color, vf.createLiteral(Serialization.colorString(box.getColor())));
        
        if (box.getBorderStyle(Side.TOP) != null && box.hasTopBorder())
        {
            IRI btop = insertBorder(box.getBorderStyle(Side.TOP), individual, "top", graph);
            graph.add(individual, BOX.hasTopBorder, btop);
        }
        if (box.getBorderStyle(Side.RIGHT) != null && box.hasRightBorder())
        {
            IRI bright = insertBorder(box.getBorderStyle(Side.RIGHT), individual, "right", graph);
            graph.add(individual, BOX.hasRightBorder, bright);
        }
        if (box.getBorderStyle(Side.BOTTOM) != null && box.hasBottomBorder())
        {
            IRI bbottom = insertBorder(box.getBorderStyle(Side.BOTTOM), individual, "bottom", graph);
            graph.add(individual, BOX.hasBottomBorder, bbottom);
        }
        if (box.getBorderStyle(Side.LEFT) != null && box.hasLeftBorder())
        {
            IRI bleft = insertBorder(box.getBorderStyle(Side.LEFT), individual, "left", graph);
            graph.add(individual, BOX.hasLeftBorder, bleft);
        }

	}
	
	private IRI insertBorder(Border border, IRI boxUri, String side, Model graph)
	{
	    IRI uri = getIriFactory().createBorderURI(boxUri, side);
	    graph.add(uri, RDF.TYPE, BOX.Border);
	    graph.add(uri, BOX.borderWidth, vf.createLiteral(border.getWidth()));
	    graph.add(uri, BOX.borderStyle, vf.createLiteral(border.getStyle().toString()));
        graph.add(uri, BOX.borderColor, vf.createLiteral(Serialization.colorString(border.getColor())));
	    return uri;
	}
	
	private IRI insertAttribute(IRI boxUri, String name, String value, Model graph)
	{
	    IRI uri = getIriFactory().createAttributeURI(boxUri, name);
	    graph.add(uri, RDFS.LABEL, vf.createLiteral(name));
	    graph.add(uri, RDF.VALUE, vf.createLiteral(value));
	    return uri;
	}

}
