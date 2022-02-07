/**
 * Query.java
 *
 * Created on 29. 4. 2021, 20:40:14 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author burgetr
 */
@Command(name = "QUERY", sortOptions = false, abbreviateSynopsis = true,
    description = "Executes a SPARQL SELECT query on the repository and creates a CSV output",
    footer = "The repository must be previously opened using the USE command. The SPARQL query "
            + "must be specified either by the -q or the -i option.")
public class Query extends CliCommand implements Callable<Integer>
{
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-q", "--query"}, paramLabel = "query", description = "SPARQL query string")
    protected String query;
    
    @Option(order = 2, names = {"-i", "--input-file"}, paramLabel = "path", description = "SPARQL input file path")
    protected File infile;
    
    @Option(order = 3, names = {"-o", "--output-file"}, paramLabel = "path", description = "output file path")
    protected File outfile;

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
            
            RDFArtifactRepository rdfRepo = (RDFArtifactRepository) repo;
            RDFStorage storage = rdfRepo.getStorage();
            
            final String queryStr;
            if (infile != null) {
                queryStr = Files.readString(infile.toPath());
            } else if (query != null) {
                queryStr = query;
            } else {
                printError("No query specified. Use -q or -i.");
                return 2;
            }
            
            if (outfile != null)
            {
                OutputStream out = new FileOutputStream(outfile);
                storage.queryExportCSV(queryStr, out);
                out.close();
                System.err.println("Query result written to " + outfile);
            }
            else
            {
                storage.queryExportCSV(queryStr, System.out);
            }
            
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return 1;
    }
    
}
