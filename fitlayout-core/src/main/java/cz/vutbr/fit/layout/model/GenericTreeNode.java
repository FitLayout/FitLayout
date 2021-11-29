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
    /** A node attribute that links to an equivalent node (e.g. produced by copying
     * a node to another tree). */
    public static String ATTR_SAME_AS = "core.node.sameAs";

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
     * Signals that some children have been added or removed or their state changed.
     */
    public void childrenChanged();
    
    /**
     * Sets a user-defined attribute for the tree node. This allows to assign multiple
     * attributes identified by their names.
     * @param name the attribute name
     * @param value the attribute value
     */
    public void addUserAttribute(String name, Object value);
    
    /**
     * Obtains the user-defined attribute value assigned to the node.
     * @param name the attribute name
     * @param clazz the class of the required attribute
     * @return an object of the given class representing the value of the attribute (application-specific)
     * or {@code null} when no such attribute is present.
     */
    public <P> P getUserAttribute(String name, Class<P> clazz);

}
