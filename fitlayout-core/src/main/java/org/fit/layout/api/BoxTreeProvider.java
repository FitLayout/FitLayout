/**
 * BoxTreeProvider.java
 *
 * Created on 27. 1. 2015, 15:09:56 by burgetr
 */
package org.fit.layout.api;

import org.fit.layout.model.Page;

/**
 * This interface represents a service that is able to provide a box tree based on further parametres.
 * 
 * @author burgetr
 */
public interface BoxTreeProvider extends Service, ParametrizedOperation
{

    /**
     * Renders the page and returns the page model.
     * @return The resulting page model or {@code null} when an error occured.
     */
    public Page getPage();
    
}
