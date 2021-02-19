/**
 * StoreArtifact.java
 *
 * Created on 15. 2. 2021, 13:43:24 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "STORE", sortOptions = false, abbreviateSynopsis = true,
    description = "Stores an artifact to the repository",
    footer = "The repository must be previously opened using the USE command")
public class StoreArtifact extends CliCommand implements Callable<Integer>
{
    
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
            
            final Artifact art = getCli().getLastArtifact();
            if (art != null)
            {
                repo.addArtifact(art);
                System.out.println("Stored: " + art);
            }
            else
                errNoArtifact("STORE");
            
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }

}
