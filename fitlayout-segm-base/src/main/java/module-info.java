module cz.vutbr.fit.layout.segm
{
    requires java.desktop;
    requires org.slf4j;

    requires cz.vutbr.fit.layout.core;

    provides cz.vutbr.fit.layout.api.AreaTreeProvider
            with cz.vutbr.fit.layout.segm.Provider;

    provides cz.vutbr.fit.layout.api.AreaTreeOperator
            with cz.vutbr.fit.layout.segm.op.FindLineOperator,
            cz.vutbr.fit.layout.segm.op.MultiLineOperator,
            cz.vutbr.fit.layout.segm.op.HomogeneousLeafOperator,
            cz.vutbr.fit.layout.segm.op.SuperAreaOperator,
            cz.vutbr.fit.layout.segm.op.SortByPositionOperator,
            cz.vutbr.fit.layout.segm.op.SortByLinesOperator,
            cz.vutbr.fit.layout.segm.op.CollapseAreasOperator,
            cz.vutbr.fit.layout.segm.op.GroupByDOMOperator,
            cz.vutbr.fit.layout.segm.op.FlattenTreeOperator;

    //provides cz.vutbr.fit.layout.gui.BrowserPlugin
    //        with cz.vutbr.fit.layout.segm.gui.SegmentatorPlugin;
}
