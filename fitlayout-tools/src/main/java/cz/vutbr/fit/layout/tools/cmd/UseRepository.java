/**
 * UseRepository.java
 *
 * Created on 15. 2. 2021, 10:54:07 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "USE", sortOptions = false, abbreviateSynopsis = true,
    description = "Opens a repository for loading or storing artifacts")
public class UseRepository extends CliCommand implements Callable<Integer>
{
    private static final String KEY_REPOSITORY = "fitlayout.rdf.repository";
    private static final String KEY_SERVER = "fitlayout.rdf.server";
    private static final String KEY_PATH = "fitlayout.rdf.path";

    enum RepositoryType { memory, local, http };
    
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-a", "--alias"}, description = "Repository alias; if a repository with the same alias was already created, it will be reused")
    protected String alias;
    
    @Option(order = 2, names = {"-d", "--disconnect"}, description = "Disconnect the repository instead of connecting it")
    protected boolean disconnect;
    
    @Option(order = 3, names = {"-s", "--suffix"}, description = "Repository path suffix to be used with the local repository (to distinguish multiple local repositories)")
    protected String pathSuffix;
    
    @Parameters(arity = "1", index = "0", description = "Repository type: ${COMPLETION-CANDIDATES}")
    protected RepositoryType repositoryType;

    @Override
    public Integer call() throws Exception
    {
        try {
            
            if (disconnect)
            {
                // try to properly close an old repository if applicable
                final ArtifactRepository oldRepo;
                if (alias != null)
                    oldRepo = getCli().getRepositories().remove(alias);
                else
                    oldRepo = getCli().getArtifactRepository();
                
                if (oldRepo != null && oldRepo instanceof RDFArtifactRepository)
                {
                    ((RDFArtifactRepository) oldRepo).disconnect();
                }
            }
            else
            {
                // open the new repository, try to reuse an existing one
                RDFArtifactRepository repo = null;
                if (alias != null)
                    repo = getCli().getRepositories().get(alias);
                
                if (repo != null)
                    System.err.println("Reusing RDF repository " + alias);
                else
                {
                    repo = createArtifactRepository();
                    if (alias != null)
                    {
                        System.err.println("  alias " + alias);
                        getCli().getRepositories().put(alias, repo);
                    }
                }
                
                getCli().setArtifactRepository(repo);
            }
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        return 1;
    }
    
    private RDFArtifactRepository createArtifactRepository()
    {
        String configPath = System.getProperty(KEY_PATH);
        if (pathSuffix != null && configPath != null)
            configPath = configPath + "-" + pathSuffix;
        String configServer = System.getProperty(KEY_SERVER);
        String configRepository = System.getProperty(KEY_REPOSITORY);
        RDFArtifactRepository storage = null;
        switch (repositoryType)
        {
            case memory:
                storage = RDFArtifactRepository.createMemory(null);
                System.err.println("Using rdf4j memory storage");
                break;
            case local:
                if (configPath == null)
                    throw new IllegalArgumentException(KEY_PATH + " system property is not set. Check your repository configuration.");
                String path = configPath.replace("$HOME", System.getProperty("user.home"));
                storage = RDFArtifactRepository.createNative(path);
                System.err.println("Using rdf4j native storage in " + path);
                break;
            case http:
                if (configServer == null)
                    throw new IllegalArgumentException(KEY_SERVER + " system property is not set. Check your repository configuration.");
                if (configRepository == null)
                    throw new IllegalArgumentException(KEY_REPOSITORY + " system property is not set. Check your repository configuration.");
                storage = RDFArtifactRepository.createHTTP(configServer, configRepository);
                System.err.println("Using rdf4j remote HTTP storage on " + configServer + " / " + configRepository);
                break;
        }
        return storage;
    }
    
}
