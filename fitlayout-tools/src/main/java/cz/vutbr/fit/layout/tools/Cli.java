/**
 * Cli.java
 *
 * Created on 7. 2. 2021, 9:29:03 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.impl.DefaultArtifactRepository;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.tools.cmd.Batch;
import cz.vutbr.fit.layout.tools.cmd.Clear;
import cz.vutbr.fit.layout.tools.cmd.Dump;
import cz.vutbr.fit.layout.tools.cmd.Export;
import cz.vutbr.fit.layout.tools.cmd.Import;
import cz.vutbr.fit.layout.tools.cmd.Invoke;
import cz.vutbr.fit.layout.tools.cmd.ListArtifacts;
import cz.vutbr.fit.layout.tools.cmd.LoadArtifact;
import cz.vutbr.fit.layout.tools.cmd.Query;
import cz.vutbr.fit.layout.tools.cmd.Renderer;
import cz.vutbr.fit.layout.tools.cmd.Segmentator;
import cz.vutbr.fit.layout.tools.cmd.StoreArtifact;
import cz.vutbr.fit.layout.tools.cmd.UseRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "fitlayout", 
    subcommands = {Renderer.class,
                   Segmentator.class,
                   Invoke.class,
                   Export.class,
                   UseRepository.class,
                   ListArtifacts.class,
                   LoadArtifact.class,
                   StoreArtifact.class,
                   Query.class,
                   Batch.class,
                   Import.class,
                   Dump.class,
                   Clear.class},
    footer = "Use COMMAND -h for getting usage information on the individual commands.")
public class Cli
{
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_FILE_TEST = "config.test.properties";

    private ArtifactRepository artifactRepository;
    private Page page;
    private AreaTree areaTree;
    private Artifact lastArtifact;
    
    
    public Cli()
    {
        //use a default in-memory repository for start, the USE command may change it later
        artifactRepository = new DefaultArtifactRepository();
    }
    
    public Cli(ArtifactRepository repo)
    {
        artifactRepository = repo;
    }
    
    /**
     * Creates a sub-shell for parallel tasks. It shares the same artifact repository and initial
     * initial values of the artifacts being processed.
     * @return a new Cli instance for a sub-task
     */
    public Cli copy()
    {
        Cli ret = new Cli(this.artifactRepository);
        ret.page = page;
        ret.areaTree = areaTree;
        ret.lastArtifact = lastArtifact;
        return ret;
    }
    
    public ArtifactRepository getArtifactRepository()
    {
        return artifactRepository;
    }

    public void setArtifactRepository(ArtifactRepository artifactRepository)
    {
        this.artifactRepository = artifactRepository;
    }

    public Page getPage()
    {
        return page;
    }

    public void setPage(Page page)
    {
        this.page = page;
        this.lastArtifact = page;
    }

    public AreaTree getAreaTree()
    {
        return areaTree;
    }

    public void setAreaTree(AreaTree areaTree)
    {
        this.areaTree = areaTree;
        this.lastArtifact = areaTree;
    }

    /**
     * Gets the last created artifact.
     * @return
     */
    public Artifact getLastArtifact()
    {
        return lastArtifact;
    }
    
    public void setLastArtifact(Artifact art)
    {
        if (art instanceof Page)
            setPage((Page) art);
        else if (art instanceof AreaTree)
            setAreaTree((AreaTree) art);
        else
            lastArtifact = art;
    }
    
    /**
     * Gets a service manager and repository for generating the artifacr IRIs
     * @return the service manager
     */
    public ServiceManager getServiceManager()
    {
        return FLConfig.createServiceManager(artifactRepository);
    }

    public int execCommandLine(String[] args)
    {
        CommandLine cmd = new CommandLine(this);
        cmd.setUsageHelpWidth(90);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        
        //init subcommands
        for (CommandLine sub : cmd.getSubcommands().values())
        {
            ((CliCommand) sub.getCommandSpec().userObject()).setCli(this);
        }
        
        //split command line to individual commands
        Set<String> cnames = cmd.getSubcommands().keySet();
        List<List<String>> subcommands = splitArgsByCommands(args, cnames);

        //execute the individual command lines
        for (List<String> subcl : subcommands)
        {
            String[] a = subcl.toArray(new String[0]);
            int exitCode = cmd.execute(a);
            if (exitCode != 0)
                return exitCode;
        }
        
        return 0;
    }

    private static List<List<String>> splitArgsByCommands(String[] args, Set<String> cnames)
    {
        List<List<String>> ret = new ArrayList<>();
        List<String> current = new ArrayList<>();
        ret.add(current);
        for (int i = 0; i < args.length; i++)
        {
            if (cnames.contains(args[i]) && !current.isEmpty())
            {
                current = new ArrayList<>();
                ret.add(current);
            }
            current.add(args[i]);
        }
        return ret;
    }

    private static void loadConfigFile()
    {
        File f = new File(CONFIG_FILE_TEST);
        if (!f.isFile())
            f = new File(CONFIG_FILE);
        if (f.isFile())
        {
            try (InputStream input = new FileInputStream(f)) 
            {
                Properties p = new Properties();
                p.load(input);
                for (String name : p.stringPropertyNames())
                {
                    String value = p.getProperty(name);
                    System.setProperty(name, value);
                }
            } catch (IOException e) {
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //load config file if present
        loadConfigFile();
        //create and exec the CLI
        Cli cli = new Cli();
        int exitCode = cli.execCommandLine(args);
        System.exit(exitCode);
    }


}
