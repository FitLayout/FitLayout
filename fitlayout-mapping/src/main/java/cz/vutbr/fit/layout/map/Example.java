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
    private IRI subjectType; //optional subject type
    
    
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
    
    public IRI getSubjectType()
    {
        return subjectType;
    }

    public void setSubjectType(IRI subjectType)
    {
        this.subjectType = subjectType;
    }

    @Override
    public String toString()
    {
        return "Example [subject=" + subject + ", predicate=" + predicate
                + ", text=" + text + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.toString().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Example other = (Example) obj;
        if (predicate == null)
        {
            if (other.predicate != null) return false;
        }
        else if (!predicate.equals(other.predicate)) return false;
        if (subject == null)
        {
            if (other.subject != null) return false;
        }
        else if (!subject.toString().equals(other.subject.toString())) return false;
        return true;
    }
    
    
}
