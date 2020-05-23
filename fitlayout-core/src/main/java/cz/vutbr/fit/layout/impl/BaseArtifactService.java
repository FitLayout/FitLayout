/**
 * BaseArtifactService.java
 *
 * Created on 23. 5. 2020, 16:44:22 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.api.ArtifactService;

/**
 * A base implementation of an artifact service.
 * 
 * @author burgetr
 */
public abstract class BaseArtifactService extends BaseParametrizedOperation implements ArtifactService
{
    
    @Override
    public String toString()
    {
        return getName() + " (" + getId() + ")";
    }

}
