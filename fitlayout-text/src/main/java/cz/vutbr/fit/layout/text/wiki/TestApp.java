/**
 * TestApp.java
 *
 * Created on 3. 12. 2025, 20:10:59 by burgetr
 */
package cz.vutbr.fit.layout.text.wiki;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

import cz.vutbr.fit.layout.text.taggers.EmbeddingTagger;

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
        String sshHost = System.getProperty(EmbeddingTagger.SSH_HOST_PROPERTY);
        String sshScriptPath = System.getProperty(EmbeddingTagger.SSH_SCRIPT_PATH_PROPERTY);
        if (sshHost != null && sshScriptPath != null) {
            SSHClient client = new SSHClient(sshHost, sshScriptPath);
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
            System.out.println("Please set the " + EmbeddingTagger.SSH_HOST_PROPERTY + " and " + EmbeddingTagger.SSH_SCRIPT_PATH_PROPERTY
                    + " system properties properly.");
        }
    }

}
