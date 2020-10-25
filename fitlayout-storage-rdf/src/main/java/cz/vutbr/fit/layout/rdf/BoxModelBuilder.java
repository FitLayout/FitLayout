package cz.vutbr.fit.layout.rdf;

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
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * Implements an RDF graph construction from a page box model. 
 * 
 * @author milicka
 * @author burgetr 
 */
public class BoxModelBuilder implements ModelBuilder
{
	private ValueFactory vf;
	
	private int next_order; //order counter

	public BoxModelBuilder() 
	{
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
		next_order = 0;
		
		// inicialization with launch node
		graph.add(pageNode, RDF.TYPE, BOX.Page);
		graph.add(pageNode,	BOX.launchDatetime,	vf.createLiteral(new java.util.Date()));
		graph.add(pageNode, BOX.sourceUrl, vf.createLiteral(baseUrl));
		if (page.getTitle() != null)
		    graph.add(pageNode, BOX.hasTitle, vf.createLiteral(page.getTitle()));

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
		final IRI individual = RESOURCE.createBoxURI(pageNode, box);
		graph.add(individual, RDF.TYPE, BOX.Box);
		graph.add(individual, BOX.documentOrder, vf.createLiteral(next_order++));

		// pin to page node
		graph.add(individual, BOX.belongsTo, pageNode);
		
		//parent
		if (box.getParent() != null)
		    graph.add(individual, BOX.isChildOf, RESOURCE.createBoxURI(pageNode, box.getParent()));

		//tag properties
		if (box.getTagName() != null)
		    graph.add(individual, BOX.htmlTagName, vf.createLiteral(box.getTagName()));
		
		//attributes
		Map<String, String> attrs = box.getAttributes();
		for (Map.Entry<String, String> attr : attrs.entrySet())
		{
		    IRI attrUri = insertAttribute(individual, attr.getKey(), attr.getValue(), graph);
		    graph.add(individual, BOX.hasAttribute, attrUri);
		}
		
		// store position and size of element
		Rectangular content = box.getContentBounds();
		graph.add(individual, BOX.height, vf.createLiteral(content.getHeight()));
		graph.add(individual, BOX.width, vf.createLiteral(content.getWidth()));
		graph.add(individual, BOX.positionX, vf.createLiteral(content.getX1()));
		graph.add(individual, BOX.positionY, vf.createLiteral(content.getY1()));
        Rectangular visual = box.getVisualBounds();
        graph.add(individual, BOX.visualHeight, vf.createLiteral(visual.getHeight()));
        graph.add(individual, BOX.visualWidth, vf.createLiteral(visual.getWidth()));
        graph.add(individual, BOX.visualX, vf.createLiteral(visual.getX1()));
        graph.add(individual, BOX.visualY, vf.createLiteral(visual.getY1()));

		if (box.getBackgroundColor() != null)
		{
            graph.add(individual, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(box.getBackgroundColor())));
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
	    IRI uri = RESOURCE.createBorderURI(boxUri, side);
	    graph.add(uri, RDF.TYPE, BOX.Border);
	    graph.add(uri, BOX.borderWidth, vf.createLiteral(border.getWidth()));
	    graph.add(uri, BOX.borderStyle, vf.createLiteral(border.getStyle().toString()));
        graph.add(uri, BOX.borderColor, vf.createLiteral(Serialization.colorString(border.getColor())));
	    return uri;
	}
	
	private IRI insertAttribute(IRI boxUri, String name, String value, Model graph)
	{
	    IRI uri = RESOURCE.createAttributeURI(boxUri, name);
	    graph.add(uri, RDFS.LABEL, vf.createLiteral(name));
	    graph.add(uri, RDF.VALUE, vf.createLiteral(value));
	    return uri;
	}

}
