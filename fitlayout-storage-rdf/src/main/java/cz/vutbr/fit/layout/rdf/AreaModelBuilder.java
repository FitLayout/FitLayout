package cz.vutbr.fit.layout.rdf;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Implements an RDF graph construction from an area tree. 
 * 
 * @author milicka
 * @author burgetr 
 */
public class AreaModelBuilder implements ModelBuilder
{
	private ValueFactory vf;
	
	private int next_order;

	public AreaModelBuilder()
	{
        vf = SimpleValueFactory.getInstance();
	}

    @Override
    public Model createGraph(Artifact artifact)
    {
        return createAreaTreeModel((AreaTree) artifact, artifact.getIri());
    }
	
	//=========================================================================
	
	private Model createAreaTreeModel(AreaTree areaTree, IRI areaTreeNode) 
	{
        Model graph = new LinkedHashModel();
        IRI pageNode = areaTree.getPageIri();
        Set<Tag> usedTags = new HashSet<Tag>();
	    
		graph.add(areaTreeNode, RDF.TYPE, SEGM.AreaTree);
		graph.add(areaTreeNode, SEGM.hasSourcePage, pageNode);
		next_order = 0;
		
		addArea(areaTree.getRoot(), areaTreeNode, pageNode, usedTags, graph);
		insertAllAreas(areaTree.getRoot().getChildren(), areaTreeNode, pageNode, usedTags, graph);
		
        addUsedTags(usedTags, graph);
        
        return graph;
	}

	/**
	 * Adds a list of areas to the model 
	 * @param areas
	 */
	private void insertAllAreas(List<Area> areas, IRI areaTreeNode, IRI pageNode, Set<Tag> usedTags, Model graph)
	{
		for(Area area : areas) 
		{
			addArea(area, areaTreeNode, pageNode, usedTags, graph);
			insertAllAreas(area.getChildren(), areaTreeNode, pageNode, usedTags, graph);
		}
	}

	/**
	 * Adds a single area and all its properties to the model.
	 * @param area
	 */
	private void addArea(Area area, IRI areaTreeNode, IRI pageNode, Set<Tag> usedTags, Model graph) 
	{
		final IRI individual = RESOURCE.createAreaURI(areaTreeNode, area);
		graph.add(individual, RDF.TYPE, SEGM.Area);
		if (area instanceof DefaultArea && ((DefaultArea) area).getName() != null)
		    graph.add(individual, RDFS.LABEL, vf.createLiteral(((DefaultArea) area).getName()));
		graph.add(individual, BOX.documentOrder, vf.createLiteral(next_order++));
        graph.add(individual, SEGM.belongsTo, areaTreeNode);

        if (area.getParent() != null)
            graph.add(individual, SEGM.isChildOf, RESOURCE.createAreaURI(areaTreeNode, area.getParent()));
        
		// appends geometry
		Rectangular rec = area.getBounds();
		graph.add(individual, BOX.height, vf.createLiteral(rec.getHeight()));
		graph.add(individual, BOX.width, vf.createLiteral(rec.getWidth()));
		graph.add(individual, BOX.positionX, vf.createLiteral(rec.getX1()));
		graph.add(individual, BOX.positionY, vf.createLiteral(rec.getY1()));

		// appends tags
		if (area.getTags().size() > 0) 
		{
			Map<Tag, Float> tags = area.getTags();
			for (Tag t : tags.keySet()) 
			{
				Float support = tags.get(t);
				if (support != null && support > 0.0f)
				{
				    usedTags.add(t);
				    final IRI tagUri = RESOURCE.createTagURI(t);
				    graph.add(individual, SEGM.hasTag, tagUri);
				    final IRI supUri = RESOURCE.createTagSupportURI(individual, t);
				    graph.add(individual, SEGM.tagSupport, supUri);
				    graph.add(supUri, SEGM.support, vf.createLiteral(support));
				    graph.add(supUri, SEGM.hasTag, tagUri);
				}
			}
		}
		
        if (area.getBackgroundColor() != null)
        {
            graph.add(individual, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(area.getBackgroundColor())));
        }

        // font attributes
        graph.add(individual, BOX.fontSize, vf.createLiteral(area.getTextStyle().getFontSize()));
        graph.add(individual, BOX.fontWeight, vf.createLiteral(area.getTextStyle().getFontWeight()));
        graph.add(individual, BOX.fontStyle, vf.createLiteral(area.getTextStyle().getFontStyle()));
        graph.add(individual, BOX.underline, vf.createLiteral(area.getTextStyle().getUnderline()));
        graph.add(individual, BOX.lineThrough, vf.createLiteral(area.getTextStyle().getLineThrough()));
        //TODO store content length - getFontWeightCnt?
        
        //dump boxes
        for (Box box : area.getBoxes())
        {
            IRI boxUri = RESOURCE.createBoxURI(pageNode, box);
            graph.add(individual, SEGM.containsBox, boxUri);
        }
	}

    private void addUsedTags(Set<Tag> usedTags, Model graph)
    {
        for (Tag t : usedTags)
        {
            IRI tagUri = RESOURCE.createTagURI(t);
            graph.add(tagUri, SEGM.hasType, vf.createLiteral(t.getType()));
            graph.add(tagUri, SEGM.hasName, vf.createLiteral(t.getValue()));
        }
    }
    
}
