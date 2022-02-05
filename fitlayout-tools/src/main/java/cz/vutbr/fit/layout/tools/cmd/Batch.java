/**
 * Batch.java
 *
 * Created on 30. 1. 2022, 16:39:50 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    
    @Option(order = 2, names = {"-d", "--delemiter"}, description = "Data field delimiter in the input data file (default \\\t)")
    protected String delim = "\\t";
    
    @Option(order = 3, names = {"-p", "--threads"}, description = "Number of threads to use for iteration (default 1)")
    protected int threads = 1;
    
    @Parameters(arity = "1", index = "0", paramLabel = "batch_file", description = "A text file containing commands to execute")
    protected String batchFile;

    int tasksToDo;
    int tasksDone;
    
    
    @Override
    public Integer call() throws Exception
    {
        try {
            String cmdString = Files.readString(Path.of(batchFile));
            if (inFile == null)
                return execCommandLine(cmdString, null);
            else
                return iterateDataFile(cmdString);
        } catch (IOException e) {
            return 1;
        }
    }

    private int execCommandLine(String cmdString, String dataLine)
    {
        if (dataLine != null)
        {
            // replace $? with data fileds fields when provided
            String[] fields = dataLine.split(delim);
            for (int i = 0; i < fields.length; i++)
            {
                cmdString = cmdString.replaceAll("\\$"+(i+1), fields[i]);
            }
            cmdString = cmdString.replaceAll("\\$0", dataLine);
        }
        List<String> cmdList = ArgumentTokenizer.tokenize(cmdString);
        String[] args = cmdList.toArray(new String[0]);
        return getCli().execCommandLine(args);
    }
    
    private int iterateDataFile(String cmdString) throws IOException
    {
        List<BatchTask> tasks = createTasks(inFile, cmdString);
        tasksToDo = tasks.size();
        tasksDone = 0;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        try
        {
            List<Future<Integer>> results = exec.invokeAll(tasks);
            for (Future<Integer> ft : results)
            {
                ft.get();
            }
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 1;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return 1;
        } finally {
            exec.shutdown();
        }
    }

    private List<BatchTask> createTasks(String listFile, String cmdString)
            throws IOException
    {
        Path filePath = Path.of(listFile);
        List<String> dataLines = Files.readAllLines(filePath);
        List<BatchTask> tasks = new LinkedList<>();
        long index = 0;
        for (String dataLine : dataLines)
            tasks.add(new BatchTask(this, index++, cmdString, dataLine));
        return tasks;
    }
    
    public synchronized void taskFinished(BatchTask task, int status)
    {
        tasksDone++;
        System.err.print((status == 0) ? "Done: " : "ERROR: ");
        System.err.println("(" + task.getIndex() + ") " + task.getDataLine());
        System.err.println(tasksDone + " / " + tasksToDo + " finished");
    }
    
    // ===============================================================
    
    private static class BatchTask implements Callable<Integer>
    {
        private long index;
        private Batch parent;
        private String cmdString;
        private String dataLine;
        
        public BatchTask(Batch parent, long index, String cmdString, String dataLine)
        {
            this.index = index;
            this.parent = parent;
            this.cmdString = cmdString;
            this.dataLine = dataLine;
        }

        public long getIndex()
        {
            return index;
        }

        public String getDataLine()
        {
            return dataLine;
        }

        @Override
        public Integer call() throws Exception
        {
            // create a separate CLI for the batch
            // we need separate service instances
            int ret = parent.execCommandLine(cmdString, dataLine);
            parent.taskFinished(this, ret);
            return ret;
        }
        
    }
    
}
