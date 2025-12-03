/**
 * TestApp.java
 *
 * Created on 3. 12. 2025, 20:10:59 by burgetr
 */
package cz.vutbr.fit.layout.text.wiki;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

/**
 * 
 * @author burgetr
 */
public class TestApp
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length >= 1) {
            SSHClient client = new SSHClient(args[0], "~/tmp/embed/predict.sh");
            try
            {
                var ret = client.runQuery("Tom Jones");
                System.out.println(ret);
                System.out.println(ret.getAsJsonObject().get("scores"));
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java TestApp <ssh_host>");
        }
    }

}
