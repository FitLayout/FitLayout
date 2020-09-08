/**
 * RDFConnectorMemory.java
 *
 * Created on 7. 9. 2020, 12:53:24 by burgetr
 */
package cz.vutbr.fit.layout.rdf.io;

import java.io.File;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * 
 * @author burgetr
 */
public class RDFConnectorMemory extends RDFConnector
{
    private String dataDir;
    
    
    public RDFConnectorMemory(String dataDir)
    {
        this.dataDir = dataDir;
        initRepository();
    }

    public String getDataDir()
    {
        return dataDir;
    }

    @Override
    protected void initRepository() throws RepositoryException
    {
        if (dataDir != null)
        {
            File data = new File(dataDir);
            repo = new SailRepository(new MemoryStore(data));
        }
        else
        {
            repo = new SailRepository(new MemoryStore());
        }
        connection = repo.getConnection();
    }

}
