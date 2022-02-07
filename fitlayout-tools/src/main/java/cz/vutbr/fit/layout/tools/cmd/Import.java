/**
 * Import.java
 *
 * Created on 5. 2. 2022, 19:03:37 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.tools.CliCommand;
import cz.vutbr.fit.layout.tools.cmd.Dump.Format;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "IMPORT", sortOptions = false, abbreviateSynopsis = true, 
    description = "Imports a RDF file to current repository")
public class Import extends CliCommand implements Callable<Integer>
{
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-f", "--format"}, paramLabel = "format",
            description = "Input file format: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE}).")
    protected Format format = Format.nquads;
    
    @Option(order = 2, names = {"-b", "--base"}, paramLabel = "base_uri",
            description = "The base URI used for import")
    protected String baseUri = null;
    
    @Option(order = 3, names = {"-c", "--context"}, paramLabel = "context_iri",
            description = "The context (IRI) to import the file to")
    protected String context = null;
    
    @Parameters(arity = "1", index = "0", paramLabel = "rdf_file", description = "The input file to import")
    protected String inputFile;

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
            
            System.err.println("Importing " + inputFile);
            InputStream is = new FileInputStream(inputFile);
            
            RDFArtifactRepository rdfRepo = (RDFArtifactRepository) repo;
            String mime = Dump.getTypeForFormat(format);
            if (context == null)
            {
                if (baseUri == null)
                    rdfRepo.getStorage().importStream(is, Serialization.getFormatForMimeType(mime));
                else
                    rdfRepo.getStorage().importStream(is, Serialization.getFormatForMimeType(mime), baseUri);
            }
            else
            {
                IRI contextIri = Values.iri(context);
                if (baseUri == null)
                    rdfRepo.getStorage().importStream(is, Serialization.getFormatForMimeType(mime), contextIri);
                else
                    rdfRepo.getStorage().importStream(is, Serialization.getFormatForMimeType(mime), contextIri, baseUri);
            }
            
            return 0;
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

}
