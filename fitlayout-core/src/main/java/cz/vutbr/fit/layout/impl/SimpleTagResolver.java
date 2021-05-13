/**
 * SimpleTagResolver.java
 *
 * Created on 8. 4. 2021, 13:09:15 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Set;

import cz.vutbr.fit.layout.api.TagResolver;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;

/**
 * A simple implementation of a tag resolver that only uses the tags assigned to the source area.
 * 
 * @author burgetr
 */
public class SimpleTagResolver implements TagResolver
{
    private float minSupport;

    public SimpleTagResolver(float minSupport)
    {
        this.minSupport = minSupport;
    }
    
    public float getMinSupport()
    {
        return minSupport;
    }

    public void setMinSupport(float minSupport)
    {
        this.minSupport = minSupport;
    }

    @Override
    public Set<Tag> getAreaTags(Area a)
    {
        return a.getSupportedTags(minSupport);
    }

}
