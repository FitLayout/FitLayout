/**
 * GenericTreeNode.java
 *
 * Created on 9. 2. 2018, 23:28:23 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

/**
 * A generic tree node interface.
 * 
 * @author burgetr
 */
public interface GenericTreeNode<T extends GenericTreeNode<T>>
{

    public T getParent();
    
    public void setParent(T parent);
    
    public T getRoot();

    public void setRoot(T root);

    public boolean isRoot();
    
    public List<T> getChildren();

    public int getChildCount();

    public boolean isLeaf();

    public void appendChild(T child);

    public void appendChildren(List<T> children);
    
    public void insertChild(T child, int index)
            throws IndexOutOfBoundsException;

    public void removeAllChildren();

    public void removeChild(int index)
            throws IndexOutOfBoundsException;

    public void removeChild(T child)
            throws IllegalArgumentException;

    public T getChildAt(int index)
            throws IndexOutOfBoundsException;
    
    public int getIndex(T child);
    
    public T getPreviousSibling();

    public T getNextSibling();

    public int getDepth();
    
    public int getLeafCount();
    
    /**
     * Sets a user-defined attribute for the tree node. This allows to assign multiple
     * attributes with different classes. One object of each class is allowed.
     * @param attribute an object representing the user attributes (application-specific)
     */
    public void addUserAttribute(Object attribute);
    
    /**
     * Obtains the user-defined attributes of the node.
     * @param clazz the class of the required attribute
     * @return an object of the given class representing the user attributes (application-specific)
     * or {@code null} when no such attribute is present.
     */
    public <P> P getUserAttribute(Class<P> clazz);

}
