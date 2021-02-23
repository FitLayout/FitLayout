/**
 * ConnectionTest.java
 *
 * Created on 7. 9. 2020, 14:03:01 by burgetr
 */
package cz.vutbr.fit.layout.rdf.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;

/**
 * 
 * @author burgetr
 */
public class ConnectionTest
{

    /**
     * Tries to initialize the repository metadata for an in-memory repository.
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    @Test
    public void checkStorageConnection() throws RDFParseException, RepositoryException, IOException
    {
        RDFArtifactRepository repo = RDFArtifactRepository.createMemory(null);
        repo.initMetadata();
        assertTrue("Repository is correctly initialized", repo.isInitialized());
    }
    
}
