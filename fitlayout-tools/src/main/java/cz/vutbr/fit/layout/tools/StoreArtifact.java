/**
 * StoreArtifact.java
 *
 * Created on 15. 2. 2021, 13:43:24 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.LoadArtifact.ArtifactType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "STORE", sortOptions = false, abbreviateSynopsis = true)
public class StoreArtifact extends CliCommand implements Callable<Integer>
{
    @Parameters(arity = "1", index = "0", description = "Repository type: ${COMPLETION-CANDIDATES}")
    protected ArtifactType artifactType;
    
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
            
            switch (artifactType)
            {
                case page:
                    if (getCli().getPage() != null)
                        repo.addArtifact(getCli().getPage());
                    else
                        errNoPage("STORE");
                    break;
                case areatree:
                    if (getCli().getAreaTree() != null)
                        repo.addArtifact(getCli().getAreaTree());
                    else
                        errNoAreaTree("STORE");
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
