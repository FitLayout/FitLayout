/**
 * AreaConnectionProvider.java
 *
 * Created on 26. 12. 2021, 22:18:40 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class AreaConnectionProvider extends ConnectionSetArtifactService
{
    private float minRelationWeight = 0.1f;
    private String method = "symmetric";
    
    private int maxDistance = 500;
    private int k = 5;
    private boolean leafOnly = true;
    

    public AreaConnectionProvider()
    {
    }

    public float getMinRelationWeight()
    {
        return minRelationWeight;
    }

    public void setMinRelationWeight(float minRelationWeight)
    {
        this.minRelationWeight = minRelationWeight;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getMaxDistance()
    {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance)
    {
        this.maxDistance = maxDistance;
    }

    public int getK()
    {
        return k;
    }

    public void setK(int k)
    {
        this.k = k;
    }

    public boolean getLeafOnly()
    {
        return leafOnly;
    }

    public void setLeafOnly(boolean leafOnly)
    {
        this.leafOnly = leafOnly;
    }

    @Override
    public String getId()
    {
        return "FitLayout.AreaConnections";
    }

    @Override
    public String getName()
    {
        return "Area connection extractor";
    }

    @Override
    public String getDescription()
    {
        return "Extracts various connections between visual areas.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterFloat("minRelationWeight", 
                "Minimal required weight of extracted relations", -1000.0f, 1000.0f));
        ret.add(new ParameterString("method", 
                "Used analysis method {symmetric, aligned, knn, visibility}", 1, 32));
        ret.add(new ParameterInt("maxDistance",
                "Maximum distance for area connections", 1, 10000));
        ret.add(new ParameterInt("k",
                "The K parameter for KNN", 1, 50));
        ret.add(new ParameterBoolean("leafOnly",
                "Only consider leaf areas for connections"));
        return ret;
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return null;
    }

    @Override
    public String getCategory()
    {
        return "Relations";
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof AreaTree)
        {
            AreaTree atree = (AreaTree) input;
            if (atree.getPageIri() != null)
            {
                Artifact page = getServiceManager().getArtifactRepository().getArtifact(atree.getPageIri());
                if (page != null && page instanceof Page)
                {
                    final var conns = extractConnections(atree, (Page) page);
                    saveConnections(input.getIri(), conns);
                    return null;
                }
                else
                    throw new ServiceException("Couldn't fetch source page");
            }
            else
                throw new ServiceException("Source page not set");
        }
        else
            throw new ServiceException("Source artifact not specified or not an area tree");
    }

    public Collection<AreaConnection> extractConnections(AreaTree input, Page page)
    {
        switch (method)
        {
            case "symmetric":
                return extractConnectionsSymmetric(input, page);
            case "aligned":
                return extractConnectionsAligned(input, page);
            case "knn":
                return extractConnectionsKNN(input, page);
            case "visibility":
                return extractConnectionsVisibility(input, page);
            default:
                throw new ServiceException("Unsupported analysis method, use {symmetric, aligned, knn, visibility}");
        }
    }
    
    public Collection<AreaConnection> extractConnectionsSymmetric(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzerSymmetric ra = new RelationAnalyzerSymmetric(page, leafAreas);
        ra.setMinRelationWeight(minRelationWeight);
        ra.extractConnections();
        return ra.getConnections();
    }

    public Collection<AreaConnection> extractConnectionsAligned(AreaTree input, Page page)
    {
        // TODO leafOnly is not supported in this method
        RelationAnalyzerAligned ra = new RelationAnalyzerAligned(input.getRoot());
        //ra.setMinRelationWeight(minRelationWeight); // TODO
        ra.extractConnections();
        return ra.getConnections();
    }
    
    public Collection<AreaConnection> extractConnectionsKNN(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzerKNN ra = new RelationAnalyzerKNN(page, leafAreas);
        ra.setMinRelationWeight(minRelationWeight);
        ra.setK(k);
        ra.setMaxDistance(maxDistance);
        ra.extractConnections();
        return ra.getConnections();
    }
    
    public Collection<AreaConnection> extractConnectionsVisibility(AreaTree input, Page page)
    {
        List<ContentRect> leafAreas = new ArrayList<>();
        findLeafAreas(input.getRoot(), leafAreas);
        
        RelationAnalyzerVisibility ra = new RelationAnalyzerVisibility(page, leafAreas);
        ra.setMinRelationWeight(minRelationWeight);
        ra.setMaxDistance(maxDistance);
        ra.extractConnections();
        if (leafOnly)
            return ra.getConnections();
        else
            return reduceConnections(input.getRoot(), ra.getConnections());
    }
    
    private void findLeafAreas(Area root, List<ContentRect> areas)
    {
        if (root.isLeaf())
            areas.add(root);
        else
        {
            for (Area child : root.getChildren())
            {
                findLeafAreas(child, areas);
            }
        }
    }
    
    /**
     * Recursively reduces the connections in a bottom-up manner (post-order traversal).
     * For each area in the tree, it first processes the children and then the area itself
     * using {@link #reduceParentConnections(Area, Collection)}.
     * 
     * @param root the root area of the subtree to process
     * @param connections the current collection of connections
     * @return the modified collection of connections
     */
    Collection<AreaConnection> reduceConnections(Area root, Collection<AreaConnection> connections)
    {
        Collection<AreaConnection> currentConnections = connections;
        //recursively for children
        for (Area child : root.getChildren())
        {
            currentConnections = reduceConnections(child, currentConnections);
        }
        //for the root
        currentConnections = reduceParentConnections(root, currentConnections);
        return currentConnections;
    }
    
    /**
     * Reduces the connetions so that the connections from outside to the child areas of the
     * specified parent are moved to the parent area.
     * @param parent the parent area
     * @param connections the connections to be reduced
     * @return a new collection of connections with duplicates removed.
     */
    private Collection<AreaConnection> reduceParentConnections(Area parent, Collection<AreaConnection> connections)
    {
        if (parent.isLeaf())
        {
            return connections; //nothing to do
        }
        
        Set<Area> children = new HashSet<>(parent.getChildren());
        Set<AreaConnection> ret = new HashSet<>();
        
        for (AreaConnection conn : connections)
        {
            final ContentRect a1 = conn.getA1();
            final ContentRect a2 = conn.getA2();
            
            boolean a1child = children.contains(a1);
            boolean a2child = children.contains(a2);
            
            if (a1child && !a2child)
            {
                // connection from child to outside -> move to parent
                var newconn = new AreaConnection(parent, a2, conn.getRelation(), conn.getWeight());
                ret.add(newconn);
            }
            else if (!a1child && a2child)
            {
                // connection from outside to child -> move to parent
                var newconn = new AreaConnection(a1, parent, conn.getRelation(), conn.getWeight());
                ret.add(newconn);
            }
            else
            {
                // connection between children or outside -> keep as is
                ret.add(conn);
            }
        }
        
        return ret;
    }

}
