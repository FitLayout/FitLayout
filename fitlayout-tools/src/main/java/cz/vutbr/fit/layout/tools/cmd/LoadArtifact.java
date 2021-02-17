/**
 * LoadArtifact.java
 *
 * Created on 15. 2. 2021, 11:32:23 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "LOAD", sortOptions = false, abbreviateSynopsis = true)
public class LoadArtifact extends CliCommand implements Callable<Integer>
{
    public enum ArtifactType { page, areatree };
    
    @Parameters(arity = "1", index = "0", description = "Artifact type: ${COMPLETION-CANDIDATES}")
    protected ArtifactType artifactType;
    
    @Parameters(arity = "1", index = "1", paramLabel = "iri", description = "Artifact IRI")
    protected String artifactIri;

    @Override
    public Integer call() throws Exception
    {
        try {
            final ArtifactRepository repo = getCli().getServiceManager().getArtifactRepository();
            if (repo == null || !(repo instanceof RDFArtifactRepository))
            {
                errNoRepo();
                return 2;
            }
            
            IRI iri = repo.getIriDecoder().decodeIri(artifactIri);
            
            switch (artifactType)
            {
                case page:
                    Page page = (Page) ((RDFArtifactRepository) repo).getArtifact(iri);
                    System.out.println("Loaded: " + page);
                    getCli().setPage(page);
                    break;
                case areatree:
                    AreaTree atree = (AreaTree) ((RDFArtifactRepository) repo).getArtifact(iri);
                    System.out.println("Loaded: " + atree);
                    getCli().setAreaTree(atree);
                    break;
            }
            
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }

}
