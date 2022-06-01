/**
 * Serialization.java
 *
 * Created on 4. 9. 2020, 13:55:39 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.MAPPING;
import cz.vutbr.fit.layout.ontology.RESOURCE;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * 
 * @author burgetr
 */
public class Serialization
{
    public static final String JSONLD = "application/ld+json";
    public static final String TURTLE = "text/turtle";
    public static final String RDFXML = "application/rdf+xml";
    public static final String NTRIPLES = "application/n-triples";
    public static final String NQUADS = "application/n-quads";
    
    public static final String SPARQL_QUERY = "application/sparql-query";
    
    public static final Set<String> rdfFormats = Set.of(JSONLD, TURTLE, RDFXML, NTRIPLES, NQUADS);
    
    
    public static String colorString(Color color)
    {
        if (color.getAlpha() == 255)
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        else
            return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color decodeHexColor(String colorStr) 
    {
        if (colorStr.length() == 7)
            return new Color(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
        else if (colorStr.length() == 9)
            return new Color(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ),
                    Integer.valueOf( colorStr.substring( 7, 9 ), 16 ) );
        else
            return Color.BLACK; //invalid color, this should not happen
    }
    
    public static String displayTypeString(Box.DisplayType type)
    {
        return type.name().toLowerCase().replace('_', '-');
    }
    
    public static Box.DisplayType decodeDisplayType(String typeStr)
    {
        try {
            return Box.DisplayType.valueOf(typeStr.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private static void configureNamespaces(RDFWriter writer)
    {
        // Default namespaces used for exports: the segm and box namespaces are renamed
        // to 'b' and 'a' and a special 'r' prefix is used for resources.
        writer.handleNamespace("rdf", RDF.NAMESPACE);
        writer.handleNamespace("rdfs", RDFS.NAMESPACE);
        writer.handleNamespace("xsd", XSD.NAMESPACE);
        writer.handleNamespace("b", BOX.NAMESPACE);
        writer.handleNamespace("a", SEGM.NAMESPACE);
        writer.handleNamespace("map", MAPPING.NAMESPACE);
        writer.handleNamespace("fl", FL.NAMESPACE);
        writer.handleNamespace("r", RESOURCE.NAMESPACE);
    }
    
    public static RDFWriter createRioWriterJsonLD(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
        writer.getWriterConfig().set(JSONLDSettings.OPTIMIZE, true);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }

    public static RDFWriter createRioWriterTurtle(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }
    
    public static RDFWriter createRioWriterXML(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }
    
    public static RDFWriter createRioWriterNTriples(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }
    
    public static RDFWriter createRioWriterNQuads(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.NQUADS, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }
    
    public static RDFWriter createRioWriter(OutputStream os, String mimeType)
    {
        RDFWriter rdfw;
        switch (mimeType)
        {
            case TURTLE:
                rdfw = createRioWriterTurtle(os);
                break;
            case RDFXML:
                rdfw = createRioWriterXML(os);
                break;
            case NTRIPLES:
                rdfw = createRioWriterNTriples(os);
                break;
            case NQUADS:
                rdfw = createRioWriterNQuads(os);
                break;
            default:
                rdfw = createRioWriterJsonLD(os);
                break;
        }
        return rdfw;
    }
    
    public static RDFFormat getFormatForMimeType(String mimeType)
    {
        switch (mimeType)
        {
            case TURTLE:
                return RDFFormat.TURTLE;
            case RDFXML:
                return RDFFormat.RDFXML;
            case NTRIPLES:
                return RDFFormat.NTRIPLES;
            case NQUADS:
                return RDFFormat.NQUADS;
            default:
                return RDFFormat.JSONLD;
        }
    }
    
    public static void statementsToStream(Repository repo, OutputStream os, String mimeType,
            Resource subj, IRI pred, Value obj, Resource... contexts)
    {
        try (RepositoryConnection con = repo.getConnection()) {
            try (RepositoryResult<Statement> statements = con.getStatements(subj, pred, obj, false, contexts)) {
                RDFWriter rdfw = createRioWriter(os, mimeType);
                for (Statement st: statements) {
                    rdfw.handleStatement(st);
                }
                rdfw.endRDF();
            }
        }
    }
    
    public static void statementsToStream(Collection<Statement> statements, OutputStream os, String mimeType)
    {
        RDFWriter rdfw = createRioWriter(os, mimeType);
        for (Statement stmt : statements)
            rdfw.handleStatement(stmt);
        rdfw.endRDF();
    }

    public static void modelToStream(Model model, OutputStream os, String mimeType)
    {
        statementsToStream(model, os, mimeType);
    }
    
}
