/**
 * Example.java
 *
 * Created on 24. 5. 2022, 12:26:44 by burgetr
 */
package cz.vutbr.fit.layout.map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

/**
 * An example ocurrence of a property surface form conneted to a subject
 * and predicate.
 * 
 * @author burgetr
 */
public class Example
{
    private Resource subject;
    private IRI predicate;
    private String text;
    
    public Example(Resource subject, IRI predicate, String text)
    {
        this.subject = subject;
        this.predicate = predicate;
        this.text = text;
    }

    public Resource getSubject()
    {
        return subject;
    }

    public IRI getPredicate()
    {
        return predicate;
    }

    public String getText()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return "Example [subject=" + subject + ", predicate=" + predicate
                + ", text=" + text + "]";
    }
}
