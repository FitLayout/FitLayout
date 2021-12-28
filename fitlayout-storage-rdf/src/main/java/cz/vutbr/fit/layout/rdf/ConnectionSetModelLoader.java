/**
 * ConnectionSerModelLoader.java
 *
 * Created on 27. 12. 2021, 20:48:17 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultRelation;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Relation;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.rdf.model.RDFConnectionSet;

/**
 * 
 * @author burgetr
 */
public class ConnectionSetModelLoader extends ModelLoaderBase implements ModelLoader
{
    private static Logger log = LoggerFactory.getLogger(ConnectionSetModelLoader.class);

    public ConnectionSetModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    @Override
    public Artifact loadArtifact(IRI artifactIri, RDFArtifactRepository artifactRepo) throws RepositoryException
    {
        return constructConnectionSet(artifactRepo, artifactIri);
    }
    
    //================================================================================================
    
    private RDFConnectionSet constructConnectionSet(RDFArtifactRepository artifactRepo, IRI csetIri) throws RepositoryException
    {
        Model artifactModel = artifactRepo.getStorage().getSubjectModel(csetIri);
        if (artifactModel.size() > 0)
        {
            ConnectionSetInfo csetInfo = new ConnectionSetInfo(artifactModel, csetIri);
            IRI parentIri = getPredicateIriValue(artifactModel, csetIri, FL.hasParentArtifact);
            RDFConnectionSet cset = new RDFConnectionSet(parentIri);
            csetInfo.applyToConnectionSet(cset);
            // load source artifact
            Artifact sourceArtifact = artifactRepo.getArtifact(cset.getSourceIri());
            // load connections data from a complete context model
            Model completeModel = artifactRepo.getStorage().getContextModel(csetIri);
            Set<AreaConnection> conns = new HashSet<>();
            loadConnections(completeModel, csetIri, sourceArtifact, conns);
            cset.setAreaConnections(conns);
            return cset;
        }
        else
            return null;
    }
    
    private void loadConnections(Model model, IRI csetIri, Artifact sourceArtifact, Set<AreaConnection> conns)
    {
        for (Statement st : model) 
        {
            if (!st.getSubject().equals(csetIri)) // consider statements that don't describe the cset itself
            {
                if (st.getSubject() instanceof IRI && st.getObject() instanceof IRI)
                {
                    // TODO reuse predefined relations from repository metadata? 
                    final String relName = getIriFactory().decodeRelationURI(st.getPredicate());
                    final ContentRect rect1 = findContentRect(sourceArtifact, (IRI) st.getSubject());
                    final ContentRect rect2 = findContentRect(sourceArtifact, (IRI) st.getObject());
                    if (relName != null && rect1 != null && rect2 != null)
                    {
                        Relation rel = new DefaultRelation(relName);
                        AreaConnection con = new AreaConnection(rect1, rect2, rel, 1.0f); //TODO weights
                        conns.add(con);
                    }
                }
            }
        }
    }
    
    private ContentRect findContentRect(Artifact sourceArtifact, IRI rectIri)
    {
        // TODO
        return null;
    }
    
}
