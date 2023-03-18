package cz.vutbr.fit.layout.cormier.impl;

import cz.vutbr.fit.layout.model.GenericTreeNode;

public class Utils {

    /**
     * @return The depth of the given node within its tree, using its {@link GenericTreeNode#getParent()} method.
     */
    public static <T extends GenericTreeNode<T>> int getDepth(T area) {
        T parent = area.getParent();
        if (parent != null) {
            return getDepth(parent) + 1;
        } else {
            return 0;
        }
    }
}
