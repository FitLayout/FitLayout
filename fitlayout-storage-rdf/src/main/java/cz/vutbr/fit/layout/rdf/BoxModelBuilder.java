package cz.vutbr.fit.layout.rdf;

import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.ContentImage;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.model.RDFContentImage;
import cz.vutbr.fit.layout.rdf.model.RDFPage;

/**
 * Implements an RDF graph construction from a page box model. 
 * 
 * @author milicka
 * @author burgetr 
 */
public class BoxModelBuilder extends ModelBuilderBase implements ModelBuilder
{
	private ValueFactory vf;
	private int objIdCnt; // content object ID counter
	
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
	    
	    Model graph = new LinkedHashModel();
	    objIdCnt = 1;
		
		// store basic page data
		addArtifactData(graph, page);
		graph.add(pageNode, BOX.width, vf.createLiteral(page.getWidth()));
        graph.add(pageNode, BOX.height, vf.createLiteral(page.getHeight()));
		graph.add(pageNode, BOX.sourceUrl, vf.createLiteral(baseUrl));
		if (page.getTitle() != null)
		    graph.add(pageNode, BOX.title, vf.createLiteral(page.getTitle()));
		if (page.getPngImage() != null)
		    graph.add(pageNode, BOX.pngImage, vf.createLiteral(Base64.getEncoder().encodeToString(page.getPngImage())));

        // recursively add the boxes
		Box root = page.getRoot();
        insertBox(root, pageNode, graph);
        insertChildBoxes(root, pageNode, graph);
        
        // additional RDF properties
        if (page instanceof RDFPage)
        {
            final Set<Statement> toadd = ((RDFPage) page).getAdditionalStatements();
            if (toadd != null)
                graph.addAll(toadd);
        }
		
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
		final IRI individual = getBoxIri(pageNode, box);
		graph.add(individual, RDF.TYPE, BOX.Box);
		graph.add(individual, BOX.documentOrder, vf.createLiteral(box.getOrder()));
        graph.add(individual, BOX.visible, vf.createLiteral(box.isVisible()));

		// pin to page node
		graph.add(individual, BOX.belongsTo, pageNode);
		
		//parent
		if (box.getParent() != null)
		    graph.add(individual, BOX.isChildOf, getBoxIri(pageNode, box.getParent()));

		//tag properties
		if (box.getSourceNodeId() != null)
		    graph.add(individual, BOX.sourceXPath, vf.createLiteral(box.getSourceNodeId()));
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
		
        // common ContentRect properties - background, borders, sameAs
        addContentRectData(graph, individual, box);
        
		if (box.getBackgroundImagePng() != null)
		{
	        final IRI objuri = getIriFactory().createContentObjectURI(pageNode, objIdCnt++);
	        final RDFContentImage image = new RDFContentImage(objuri);
	        image.setPngData(box.getBackgroundImagePng());
	        insertImage(graph, image, objuri);
		    graph.add(individual, BOX.hasBackgroundImage, objuri);
		}

		// add text content into element
		if (box.getType() == Type.TEXT_CONTENT) 
		{
			graph.add(individual, BOX.text, vf.createLiteral(box.getText()));
		}
		else if (box.getType() == Type.REPLACED_CONTENT)
		{
		    final ContentObject obj = box.getContentObject();
            final IRI objuri = getIriFactory().createContentObjectURI(pageNode, objIdCnt++);
            if (obj instanceof ContentImage)
            {
                insertImage(graph, (ContentImage) obj, objuri);
                graph.add(individual, BOX.containsObject, objuri);
            }
            else
            {
                graph.add(objuri, RDF.TYPE, BOX.ContentObject);
                graph.add(individual, BOX.containsObject, objuri);
            }
		}
		// font attributes
        graph.add(individual, BOX.color, vf.createLiteral(Serialization.colorString(box.getColor())));
		graph.add(individual, BOX.fontFamily, vf.createLiteral(box.getFontFamily()));
		addTextStyle(graph, individual, box);
        
	}

    private void insertImage(Model graph, ContentImage image, IRI imageIri)
    {
        graph.add(imageIri, RDF.TYPE, BOX.Image);
        final java.net.URL url = image.getUrl();
        if (url != null)
            graph.add(imageIri, BOX.imageUrl, vf.createLiteral(url.toString()));
        final byte[] imageData = image.getPngData();
        if (imageData != null)
            graph.add(imageIri, BOX.imageData, vf.createLiteral(Base64.getEncoder().encodeToString(imageData)));
    }
	
	private IRI insertAttribute(IRI boxUri, String name, String value, Model graph)
	{
	    IRI uri = getIriFactory().createAttributeURI(boxUri, name);
	    graph.add(uri, RDFS.LABEL, vf.createLiteral(name));
	    graph.add(uri, RDF.VALUE, vf.createLiteral(value));
	    return uri;
	}

}
