/**
 * ModelBuilderBase.java
 *
 * Created on 30. 10. 2020, 16:35:44 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextStyle;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.rdf.model.RDFResource;

/**
 * Common model builder methods.
 * 
 * @author burgetr
 */
public class ModelBuilderBase extends ModelTransformer
{
    private ValueFactory vf;
    
    
    public ModelBuilderBase(IRIFactory iriFactory)
    {
        super(iriFactory);
        vf = SimpleValueFactory.getInstance();
    }

    public ValueFactory getValueFactory()
    {
        return vf;
    }

    /**
     * Stores the common information about an artifact to a model.
     * 
     * @param graph the model to store the data to
     * @param a the artifact
     */
    public void addArtifactData(Model graph, Artifact a)
    {
        final IRI node = a.getIri();
        graph.add(node, RDF.TYPE, a.getArtifactType());
        if (a.getParentIri() != null)
            graph.add(node, FL.hasParentArtifact, a.getParentIri());
        if (a.getLabel() != null)
            graph.add(node, RDFS.LABEL, vf.createLiteral(a.getLabel()));
        if (a.getCreatedOn() != null)
            graph.add(node, FL.createdOn, vf.createLiteral(a.getCreatedOn()));
        if (a.getCreator() != null)
            graph.add(node, FL.creator, vf.createLiteral(a.getCreator()));
        if (a.getCreatorParams() != null)
            graph.add(node, FL.creatorParams, vf.createLiteral(a.getCreatorParams()));
    }
    
    /**
     * Stores the common information about a content rectangle.
     * 
     * @param graph the model to add the data to
     * @param rectIri rectangle IRI
     * @param rect the rectangle to store
     */
    public void addContentRectData(Model graph, IRI rectIri, ContentRect rect)
    {
        if (rect.getBackgroundColor() != null)
        {
            graph.add(rectIri, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(rect.getBackgroundColor())));
        }
        graph.add(rectIri, BOX.backgroundSeparated, vf.createLiteral(rect.isBackgroundSeparated()));

        insertBorders(rect, rectIri, graph);
        insertSameAs(rect, rectIri, graph);
    }
    
    public void addTextStyle(Model graph, IRI rectIri, ContentRect rect)
    {
        final TextStyle textStyle = rect.getTextStyle();
        graph.add(rectIri, BOX.fontSize, vf.createLiteral(textStyle.getFontSize()));
        graph.add(rectIri, BOX.fontWeight, vf.createLiteral(textStyle.getFontWeight()));
        graph.add(rectIri, BOX.fontStyle, vf.createLiteral(textStyle.getFontStyle()));
        graph.add(rectIri, BOX.underline, vf.createLiteral(textStyle.getUnderline()));
        graph.add(rectIri, BOX.lineThrough, vf.createLiteral(textStyle.getLineThrough()));
        graph.add(rectIri, BOX.contentLength, vf.createLiteral(textStyle.getContentLength()));
    }
    
    public IRI insertBounds(IRI boxIri, IRI property, String type, Rectangular bounds, Model graph)
    {
        final IRI iri = getIriFactory().createBoundsURI(boxIri, type);
        graph.add(boxIri, property, iri);
        graph.add(iri, BOX.positionX, vf.createLiteral(bounds.getX1()));
        graph.add(iri, BOX.positionY, vf.createLiteral(bounds.getY1()));
        graph.add(iri, BOX.width, vf.createLiteral(bounds.getWidth()));
        graph.add(iri, BOX.height, vf.createLiteral(bounds.getHeight()));
        return iri;
    }
    
    public void insertBorders(ContentRect box, final IRI boxIri, Model graph)
    {
        if (box.getBorderStyle(Side.TOP) != null && box.hasTopBorder())
        {
            IRI btop = insertBorder(box.getBorderStyle(Side.TOP), boxIri, "top", graph);
            graph.add(boxIri, BOX.hasTopBorder, btop);
        }
        if (box.getBorderStyle(Side.RIGHT) != null && box.hasRightBorder())
        {
            IRI bright = insertBorder(box.getBorderStyle(Side.RIGHT), boxIri, "right", graph);
            graph.add(boxIri, BOX.hasRightBorder, bright);
        }
        if (box.getBorderStyle(Side.BOTTOM) != null && box.hasBottomBorder())
        {
            IRI bbottom = insertBorder(box.getBorderStyle(Side.BOTTOM), boxIri, "bottom", graph);
            graph.add(boxIri, BOX.hasBottomBorder, bbottom);
        }
        if (box.getBorderStyle(Side.LEFT) != null && box.hasLeftBorder())
        {
            IRI bleft = insertBorder(box.getBorderStyle(Side.LEFT), boxIri, "left", graph);
            graph.add(boxIri, BOX.hasLeftBorder, bleft);
        }
    }

    public IRI insertBorder(Border border, IRI boxUri, String side, Model graph)
    {
        IRI uri = getIriFactory().createBorderURI(boxUri, side);
        graph.add(uri, RDF.TYPE, BOX.Border);
        graph.add(uri, BOX.borderWidth, vf.createLiteral(border.getWidth()));
        graph.add(uri, BOX.borderStyle, vf.createLiteral(border.getStyle().toString()));
        graph.add(uri, BOX.borderColor, vf.createLiteral(Serialization.colorString(border.getColor())));
        return uri;
    }
    
    /**
     * Adds owl:sameAs links to referenced objects if they are RDF resources.
     * @param node the content rect being stored 
     * @param nodeIri the target IRI of the content rect
     * @param graph The RDF graph to add the links to.
     */
    public void insertSameAs(ContentRect node, IRI nodeIri, Model graph)
    {
        final Object obj = node.getUserAttribute(ContentRect.ATTR_SAME_AS, Object.class);
        if (obj != null && obj instanceof RDFResource)
        {
            final IRI objIri = ((RDFResource) obj).getIri();
            if (objIri != null)
            {
                graph.add(nodeIri, OWL.SAMEAS, objIri);
                graph.add(objIri, OWL.SAMEAS, nodeIri);
            }
        }
    }
    
}
