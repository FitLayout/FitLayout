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
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "LOAD", sortOptions = false, abbreviateSynopsis = true,
    description = "Loads an artifact from the repository",
    footer = "The repository must be previously opened using the USE command")
public class LoadArtifact extends CliCommand implements Callable<Integer>
{
    @Parameters(arity = "1", index = "0", paramLabel = "iri", description = "Artifact IRI")
    protected String artifactIri;

    @Override
    public Integer call() throws Exception
    {
        try {
            final ArtifactRepository repo = getCli().getArtifactRepository();
            if (repo == null || !(repo instanceof RDFArtifactRepository))
            {
                errNoRepo();
                return 2;
            }
            
            final IRI iri = repo.getIriDecoder().decodeIri(artifactIri);
            Artifact art = repo.getArtifact(iri);
            if (art != null)
            {
                if (art instanceof Page)
                {
                    System.err.println("Loaded page: " + art);
                    getCli().setPage((Page) art);
                }
                else if (art instanceof AreaTree)
                {
                    System.err.println("Loaded area tree: " + art);
                    getCli().setAreaTree((AreaTree) art);
                }
                else
                {
                    System.err.println("Unknwon artifact type");
                    return 1;
                }
            }
            else
            {
                System.err.println("Couldn't load artifact " + iri);
                return 1;
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
