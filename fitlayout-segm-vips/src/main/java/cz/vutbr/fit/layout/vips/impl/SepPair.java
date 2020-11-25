package cz.vutbr.fit.layout.vips.impl;

/**
 * A pair of visual structures separated by a separator
 * 
 * @author burgetr
 */
class SepPair
{
    public VisualStructure a;
    public VisualStructure b;
    public Separator separator;
    
    public boolean isComplete()
    {
        return (a != null && b != null);
    }
    
}