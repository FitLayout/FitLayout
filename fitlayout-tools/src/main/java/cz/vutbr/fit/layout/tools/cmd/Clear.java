/**
 * Clear.java
 *
 * Created on 29. 6. 2022, 13:10:59 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "CLEAR", sortOptions = false, abbreviateSynopsis = true,
description = "Clears the repository",
footer = "The repository must have been previously opened using the USE command")
public class Clear extends CliCommand implements Callable<Integer>
{

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
            
            repo.clear();
            
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }
    
}
