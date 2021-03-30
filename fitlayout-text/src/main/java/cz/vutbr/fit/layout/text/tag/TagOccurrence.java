/**
 * TagOccurrence.java
 *
 * Created on 30. 10. 2018, 16:17:23 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

/**
 * An occurrence of a taggable substring in a text string.
 * 
 * @author burgetr
 */
public class TagOccurrence
{
    private String text;
    private int position;
    private float support;
    
    public TagOccurrence(String text, int position, float support)
    {
        this.text = text;
        this.position = position;
        this.support = support;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public float getSupport()
    {
        return support;
    }

    public void setSupport(float support)
    {
        this.support = support;
    }
    
    public int getLength()
    {
        return text.length();
    }

    @Override
    public String toString()
    {
        return "('" + text + "':" + position + ":" + support + ")";
    }
    
}
