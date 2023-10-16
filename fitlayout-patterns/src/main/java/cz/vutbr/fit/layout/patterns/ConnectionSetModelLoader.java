/**
 * ConnectionSerModelLoader.java
 *
 * Created on 27. 12. 2021, 20:48:17 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.impl.DefaultRelation;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Relation;
import cz.vutbr.fit.layout.rdf.IRIFactory;
import cz.vutbr.fit.layout.rdf.ModelLoaderBase;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;
import cz.vutbr.fit.layout.rdf.model.RDFArtifact;
import cz.vutbr.fit.layout.rdf.model.RDFChunkSet;

/**
 * 
 * @author burgetr
 */
public class ConnectionSetModelLoader extends ModelLoaderBase
{
    private static Logger log = LoggerFactory.getLogger(ConnectionSetModelLoader.class);

    public ConnectionSetModelLoader(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    public Collection<AreaConnection> constructConnectionSet(IRI csetIri, RDFArtifactRepository artifactRepo) throws RepositoryException
    {
        Model artifactModel = artifactRepo.getStorage().getSubjectModel(csetIri);
        if (artifactModel.size() > 0)
        {
            // load source artifact
            Artifact sourceArtifact = artifactRepo.getArtifact(csetIri);
            // load connections data from a complete context model
            Set<AreaConnection> conns = new HashSet<>();
            loadConnections(sourceArtifact, artifactRepo, conns);
            return conns;
        }
        else
            return null;
    }
    
    private void loadConnections(Artifact sourceArtifact, RDFArtifactRepository artifactRepo, Set<AreaConnection> conns)
    {
        if (sourceArtifact instanceof RDFArtifact)
        {
            try {
                final String query = 
                        "PREFIX segm: <http://fitlayout.github.io/ontology/segmentation.owl#>\n"
                        + "PREFIX r: <http://fitlayout.github.io/resource/>\n"
                        + "SELECT ?a ?b ?r ?w WHERE {\n"
                        + "  ?a segm:belongsTo <" + sourceArtifact.getIri().toString() + ">\n"
                        + "  ?a segm:isInRelation ?descr .\n"
                        + "  ?descr segm:hasRelatedRect ?b .\n"
                        + "  ?descr segm:hasRelationType ?r .\n"
                        + "  ?descr segm:support ?w\n"
                        + "}\n";
                
                List<BindingSet> data = artifactRepo.getStorage().executeSafeTupleQuery(query);
                for (BindingSet binding : data)
                {
                    final Value va = binding.getBinding("a").getValue();
                    final Value vb = binding.getBinding("b").getValue();
                    final Value vr = binding.getBinding("r").getValue();
                    final Value vw = binding.getBinding("w").getValue();
                    if (va != null && vb != null && vr != null && vw != null
                        && va.isIRI() && vb.isIRI() && vr.isIRI() && vw.isLiteral())
                    {               
                        float w = 1.0f;
                        try {
                            w = ((Literal) vw).floatValue();
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        final String relName = getIriFactory().decodeRelationURI((IRI) vr);
                        final ContentRect rect1 = findContentRect((RDFArtifact) sourceArtifact, (IRI) va);
                        final ContentRect rect2 = findContentRect((RDFArtifact) sourceArtifact, (IRI) vb);
                        if (relName != null && rect1 != null && rect2 != null)
                        {
                            Relation rel = new DefaultRelation(relName);
                            AreaConnection con = new AreaConnection(rect1, rect2, rel, w);
                            conns.add(con);
                        }
                    }
                }
            } catch (RDF4JException e) {
                throw new StorageException(e);
            }
        }
        else
            log.warn("Source artifact {} is not RDFArtifact", sourceArtifact.getIri());
    }
    
    private ContentRect findContentRect(RDFArtifact sourceArtifact, IRI rectIri)
    {
        if (sourceArtifact instanceof RDFAreaTree)
            return ((RDFAreaTree) sourceArtifact).findAreaByIri(rectIri);
        else if (sourceArtifact instanceof RDFChunkSet)
            return ((RDFChunkSet) sourceArtifact).findTextChunkByIri(rectIri);
        else
            return null;
    }
    
}
