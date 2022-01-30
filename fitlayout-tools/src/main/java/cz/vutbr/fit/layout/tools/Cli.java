/**
 * Cli.java
 *
 * Created on 7. 2. 2021, 9:29:03 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.bcs.BCSProvider;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.impl.DefaultArtifactRepository;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
import cz.vutbr.fit.layout.segm.BasicSegmProvider;
import cz.vutbr.fit.layout.tools.cmd.Batch;
import cz.vutbr.fit.layout.tools.cmd.Export;
import cz.vutbr.fit.layout.tools.cmd.Invoke;
import cz.vutbr.fit.layout.tools.cmd.ListArtifacts;
import cz.vutbr.fit.layout.tools.cmd.LoadArtifact;
import cz.vutbr.fit.layout.tools.cmd.Query;
import cz.vutbr.fit.layout.tools.cmd.Renderer;
import cz.vutbr.fit.layout.tools.cmd.Segmentator;
import cz.vutbr.fit.layout.tools.cmd.StoreArtifact;
import cz.vutbr.fit.layout.tools.cmd.UseRepository;
import cz.vutbr.fit.layout.vips.VipsProvider;
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
                   Batch.class},
    footer = "Use COMMAND -h for getting usage information on the individual commands.")
public class Cli
{
    private static final String CONFIG_FILE = "config.properties";

    private Page page;
    private AreaTree areaTree;
    private Artifact lastArtifact;
    private ServiceManager serviceManager;
    
    
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
     * Creates a basic service manager and repository for generating the artifacr IRIs
     * @return the service manager
     */
    public ServiceManager getServiceManager()
    {
        if (serviceManager == null)
        {
            serviceManager = ServiceManager.create();
            //initialize the services
            CSSBoxTreeProvider cssboxProvider = new CSSBoxTreeProvider();
            serviceManager.addArtifactService(cssboxProvider);
            
            PuppeteerTreeProvider puppeteerProvider = new PuppeteerTreeProvider();
            serviceManager.addArtifactService(puppeteerProvider);
            
            BasicSegmProvider segmProvider = new BasicSegmProvider();
            serviceManager.addArtifactService(segmProvider);
            
            VipsProvider vipsProvider = new VipsProvider();
            serviceManager.addArtifactService(vipsProvider);
            
            BCSProvider bcsProvider = new BCSProvider();
            serviceManager.addArtifactService(bcsProvider);
            
            //use a default in-memory repository
            serviceManager.setArtifactRepository(new DefaultArtifactRepository());
        }
        return serviceManager;
    }

    public static List<List<String>> splitArgsByCommands(String[] args, Set<String> cnames)
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
        try (InputStream input = new FileInputStream(CONFIG_FILE)) 
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
    
    public static int execCommandLine(String[] args)
    {
        Cli cli = new Cli();
        
        CommandLine cmd = new CommandLine(cli);
        cmd.setUsageHelpWidth(90);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        
        //init subcommands
        for (CommandLine sub : cmd.getSubcommands().values())
        {
            ((CliCommand) sub.getCommandSpec().userObject()).setCli(cli);
        }
        
        //split command line to individual commands
        Set<String> cnames = cmd.getSubcommands().keySet();
        List<List<String>> subcommands = splitArgsByCommands(args, cnames);

        //load config file if present
        loadConfigFile();
        
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
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        int exitCode = execCommandLine(args);
        System.exit(exitCode);
    }


}
