/**
 * ListArtifacts.java
 *
 * Created on 15. 2. 2021, 13:58:36 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "LIST", sortOptions = false, abbreviateSynopsis = true,
    description = "Lists the repository contents",
    footer = "The repository must be previously opened using the USE command")
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
            
            IRIDecoder dec = repo.getIriDecoder();
            
            Collection<Artifact> iris = repo.getArtifactInfo();
            for (Artifact a : iris)
                System.out.printf("%s\t%s\n", a.getIri(), dec.encodeIri(a.getArtifactType()));
            
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }

}
