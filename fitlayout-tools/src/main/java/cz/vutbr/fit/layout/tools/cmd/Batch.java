/**
 * Batch.java
 *
 * Created on 30. 1. 2022, 16:39:50 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import cz.vutbr.fit.layout.tools.CliCommand;
import cz.vutbr.fit.layout.tools.util.ArgumentTokenizer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author burgetr
 */
@Command(name = "BATCH", sortOptions = false, abbreviateSynopsis = true,
    description = "Executes commands specified in an external file")
public class Batch extends CliCommand implements Callable<Integer>
{
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-i", "--data"}, description = "Input data file to iterate on")
    protected String inFile;
    
    @Option(order = 2, names = {"-p", "--threads"}, description = "Number of threads to use for iteration (default 1)")
    protected int threads;
    
    @Parameters(arity = "1", index = "0", paramLabel = "batch_file", description = "A text file containing commands to execute")
    protected String batchFile;

    @Override
    public Integer call() throws Exception
    {
        String cmdString = Files.readString(Path.of(batchFile));
        return execCommandLine(cmdString);
    }

    private int execCommandLine(String cmdString)
    {
        List<String> cmdList = ArgumentTokenizer.tokenize(cmdString);
        String[] args = cmdList.toArray(new String[0]);
        return getCli().execCommandLine(args);
    }
    
}
