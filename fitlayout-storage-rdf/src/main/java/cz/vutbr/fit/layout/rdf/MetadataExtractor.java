/**
 * MetadataExtractor.java
 *
 * Created on 20. 5. 2022, 13:27:07 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Metadata;

/**
 * Functions for extracting RDF metadata from source objects.
 *  
 * @author burgetr
 */
public class MetadataExtractor
{
    private static Logger log = LoggerFactory.getLogger(MetadataExtractor.class);

    /**
     * Extracts RDF metadata from a source artifact (e.g. JSON-LD metadata for a page)
     * 
     * @param artifact the source artifact
     * @return RDF model of the extracted metadata (may be empty if no metadata was found)
     */
    public static Model extract(Artifact artifact)
    {
        Model model = new LinkedHashModel();
        if (artifact.getMetadata() != null)
        {
            for (Metadata metadata : artifact.getMetadata())
            {
                if (Serialization.JSONLD.equals(metadata.getType()) && metadata.getContent() instanceof String)
                    parseJSON((String) metadata.getContent(), model);
                else
                    log.warn("Skipping unknown metadata of type {}", metadata.getType());
            }
        }
        return model;
    }

    private static void parseJSON(String src, Model model)
    {
        RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
        rdfParser.setRDFHandler(new StatementCollector(model));
        try
        {
            rdfParser.parse(new StringReader(src));
        } catch (RDFParseException | RDFHandlerException | IOException e) {
            log.error("Could not parse metadata: {}", e.getMessage());
        }
    }
    
}
