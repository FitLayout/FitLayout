/**
 * Dump.java
 *
 * Created on 31. 1. 2022, 13:31:01 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author burgetr
 */
@Command(name = "DUMP", sortOptions = false, abbreviateSynopsis = true, 
    description = "Dumps the entire repository to a file")
public class Dump extends CliCommand implements Callable<Integer>
{
    public enum Format { xml, turtle, ntriples, nquads };

    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-o", "--output-file"}, paramLabel = "path", description = "output file path (uses stdout when not specified)")
    protected File outfile;

    @Option(order = 2, names = {"-f", "--format"}, paramLabel = "format",
            description = "Output format: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE}).")
    protected Format format = Format.nquads;

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
            
            System.err.println("Repository dump");
            final OutputStream os;
            if (outfile == null)
                os = new BufferedOutputStream(System.out);
            else
                os = new FileOutputStream(outfile);
            
            RDFArtifactRepository rdfRepo = (RDFArtifactRepository) repo;
            String mime = getTypeForFormat(format);
            Serialization.statementsToStream(rdfRepo.getStorage().getRepository(), os, mime,
                    null, null, null);
            
            return 0;
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

    public static String getTypeForFormat(Format format)
    {
        String mime = Serialization.NQUADS;
        switch (format) {
            case nquads:
                mime = Serialization.NQUADS;
                break;
            case turtle:
                mime = Serialization.TURTLE;
                break;
            case xml:
                mime = Serialization.RDFXML;
                break;
            case ntriples:
                mime = Serialization.NTRIPLES;
                break;
        }
        return mime;
    }

}
