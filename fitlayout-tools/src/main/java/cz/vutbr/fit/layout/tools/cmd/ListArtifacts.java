/**
 * ListArtifacts.java
 *
 * Created on 15. 2. 2021, 13:58:36 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "LIST", sortOptions = false, abbreviateSynopsis = true,
    description = "Lists the repository contents")
public class ListArtifacts extends CliCommand implements Callable<Integer>
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
            
            Collection<IRI> iris = repo.getArtifactIRIs();
            System.out.println("IRIs:");
            for (IRI iri : iris)
                System.out.println(iri);
            
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }

}
