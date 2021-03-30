/**
 * LogicalAreaModelBuilder.java
 *
 * Created on 21. 9. 2020, 12:43:27 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.LogicalArea;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFArea;

/**
 * TODO check the belongsTo relations and the assignment ot the LogicalAreaTree artifact
 * @author burgetr
 */
public class LogicalAreaModelBuilder extends ModelBuilderBase implements ModelBuilder
{
    private ValueFactory vf;
    
    private int next_order;

    public LogicalAreaModelBuilder(IRIFactory iriFactory)
    {
        super(iriFactory);
        vf = SimpleValueFactory.getInstance();
    }

    @Override
    public Model createGraph(Artifact artifact)
    {
        return createAreaTreeModel((LogicalAreaTree) artifact, artifact.getIri());
    }
    
    //=========================================================================
    

    private Model createAreaTreeModel(LogicalAreaTree logicalTree, IRI logicalAreaTreeNode) 
    {
        IRI areaTreeNode = logicalTree.getAreaTreeIri();
        Model graph = new LinkedHashModel();
        
        graph.add(logicalAreaTreeNode, RDF.TYPE, SEGM.LogicalAreaTree);
        graph.add(logicalAreaTreeNode, SEGM.hasAreaTree, areaTreeNode);
        next_order = 0;
        
        IRI lroot = addLogicalArea(logicalTree.getRoot(), null, areaTreeNode, graph);
        insertAllLogicalAreas(logicalTree.getRoot().getChildren(), lroot, areaTreeNode, graph);
        
        return graph;
    }
    
    /**
     * Adds a list of logical areas to the model 
     * @param areas
     * @param parent
     */
    private void insertAllLogicalAreas(List<LogicalArea> areas, IRI parent, IRI areaTreeNode, Model graph) 
    {
        for (LogicalArea area : areas) 
        {
            IRI p = addLogicalArea(area, parent, areaTreeNode, graph);
            insertAllLogicalAreas(area.getChildren(), p, areaTreeNode, graph);
        }
    }

    private IRI addLogicalArea(LogicalArea area, IRI parent, IRI areaTreeNode, Model graph) 
    {
        final IRI individual = getIriFactory().createLogicalAreaURI(areaTreeNode, next_order++);
        graph.add(individual, RDF.TYPE, SEGM.LogicalArea);
        graph.add(individual, BOX.documentOrder, vf.createLiteral(next_order++));
        graph.add(individual, SEGM.belongsTo, areaTreeNode);
        graph.add(individual, SEGM.hasText, vf.createLiteral(area.getText()));
        if (parent != null)
            graph.add(individual, SEGM.isSubordinateTo, parent);
        if (area.getMainTag() != null)
            graph.add(individual, SEGM.hasTag, getIriFactory().createTagURI(area.getMainTag()));
        for (Area a : area.getAreas())
        {
            IRI areaUri;
            if (a instanceof RDFArea)
                areaUri = ((RDFArea) a).getIri();
            else
                areaUri = getIriFactory().createAreaURI(areaTreeNode, a);
            graph.add(individual, SEGM.containsArea, areaUri);
        }
        return individual;
    }
    

    
}
