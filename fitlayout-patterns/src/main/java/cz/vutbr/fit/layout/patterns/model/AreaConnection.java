/**
 * AreaConnection.java
 *
 * Created on 28. 2. 2016, 18:45:16 by burgetr
 */
package cz.vutbr.fit.layout.patterns.model;

import cz.vutbr.fit.layout.model.ContentRect;

/**
 * 
 * @author burgetr
 */
public class AreaConnection extends Connection<ContentRect>
{

    public AreaConnection(ContentRect a1, ContentRect a2, Relation relation, float weight)
    {
        super(a1, a2, relation, weight);
    }
    
}
