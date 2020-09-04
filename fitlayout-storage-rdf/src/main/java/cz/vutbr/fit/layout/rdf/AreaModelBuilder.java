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
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFArea;

/**
 * Implements an RDF graph construction from an area tree. 
 * 
 * @author milicka
 * @author burgetr 
 */
public class AreaModelBuilder
{
	private Model graph = null;
	private ValueFactory vf;
	private IRI pageNode;
	private IRI areaTreeNode;
	private int logAreaCnt;
	private Set<Tag> usedTags;
	
	private int next_order;

	public AreaModelBuilder(AreaTree areaTree, LogicalAreaTree logicalTree, IRI pageNode, IRI uri)
	{
		graph = new LinkedHashModel();
		vf = SimpleValueFactory.getInstance();
		this.pageNode = pageNode;
		areaTreeNode = uri;
		usedTags = new HashSet<Tag>();
		createAreaTreeModel(pageNode, areaTree, logicalTree);
		addUsedTags();
	}

	public Model getGraph()
	{
		return graph;
	}
	
	//=========================================================================
	
	private void createAreaTreeModel(IRI pageNode, AreaTree areaTree, LogicalAreaTree logicalTree) 
	{
		graph.add(areaTreeNode, RDF.TYPE, SEGM.AreaTree);
		graph.add(areaTreeNode, SEGM.hasSourcePage, pageNode);
		next_order = 0;
		
		addArea(areaTree.getRoot());
		insertAllAreas(areaTree.getRoot().getChildren());
		
		if (logicalTree != null)
		{
    		IRI lroot = addLogicalArea(logicalTree.getRoot(), null);
    		insertAllLogicalAreas(logicalTree.getRoot().getChildren(), lroot);
		}
	}

	/**
	 * Adds a list of areas to the model 
	 * @param areas
	 */
	private void insertAllAreas(List<Area> areas) 
	{
		for(Area area : areas) 
		{
			addArea(area);
			insertAllAreas(area.getChildren());
		}
	}

    /**
     * Adds a list of logical areas to the model 
     * @param areas
     * @param parent
     */
    private void insertAllLogicalAreas(List<LogicalArea> areas, IRI parent) 
    {
        for (LogicalArea area : areas) 
        {
            IRI p = addLogicalArea(area, parent);
            insertAllLogicalAreas(area.getChildren(), p);
        }
    }

	/**
	 * Adds a single area and all its properties to the model.
	 * @param area
	 */
	private void addArea(Area area) 
	{
		final IRI individual = RESOURCE.createAreaURI(areaTreeNode, area);
		graph.add(individual, RDF.TYPE, SEGM.Area);
		if (area instanceof DefaultArea && ((DefaultArea) area).getName() != null)
		    graph.add(individual, RDFS.LABEL, vf.createLiteral(((DefaultArea) area).getName()));
		graph.add(individual, BOX.documentOrder, vf.createLiteral(next_order++));
        graph.add(individual, SEGM.belongsTo, this.areaTreeNode);

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
        graph.add(individual, BOX.fontSize, vf.createLiteral(area.getFontSize()));
        graph.add(individual, BOX.fontWeight, vf.createLiteral(area.getFontWeight()));
        graph.add(individual, BOX.fontStyle, vf.createLiteral(area.getFontStyle()));
        graph.add(individual, BOX.underline, vf.createLiteral(area.getUnderline()));
        graph.add(individual, BOX.lineThrough, vf.createLiteral(area.getLineThrough()));
        
        //dump boxes
        for (Box box : area.getBoxes())
        {
            IRI boxUri = RESOURCE.createBoxURI(pageNode, box);
            graph.add(individual, SEGM.containsBox, boxUri);
        }
	}

    private IRI addLogicalArea(LogicalArea area, IRI parent) 
    {
        final IRI individual = RESOURCE.createLogicalAreaURI(areaTreeNode, logAreaCnt++);
        graph.add(individual, RDF.TYPE, SEGM.LogicalArea);
        graph.add(individual, BOX.documentOrder, vf.createLiteral(next_order++));
        graph.add(individual, SEGM.belongsTo, areaTreeNode);
        graph.add(individual, SEGM.hasText, vf.createLiteral(area.getText()));
        if (parent != null)
            graph.add(individual, SEGM.isSubordinateTo, parent);
        if (area.getMainTag() != null)
            graph.add(individual, SEGM.hasTag, RESOURCE.createTagURI(area.getMainTag()));
        for (Area a : area.getAreas())
        {
            IRI areaUri;
            if (a instanceof RDFArea)
                areaUri = ((RDFArea) a).getIri();
            else
                areaUri = RESOURCE.createAreaURI(areaTreeNode, a);
            graph.add(individual, SEGM.containsArea, areaUri);
        }
        return individual;
    }
    
    private void addUsedTags()
    {
        for (Tag t : usedTags)
        {
            IRI tagUri = RESOURCE.createTagURI(t);
            graph.add(tagUri, SEGM.hasType, vf.createLiteral(t.getType()));
            graph.add(tagUri, SEGM.hasName, vf.createLiteral(t.getValue()));
        }
    }
    
}
