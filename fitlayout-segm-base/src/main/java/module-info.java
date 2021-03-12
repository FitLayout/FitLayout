module cz.vutbr.fit.layout.segm
{
    requires java.desktop;
    requires org.slf4j;

    requires transitive cz.vutbr.fit.layout.core;
    requires rdf4j.onejar;
    
    exports cz.vutbr.fit.layout.segm;
    exports cz.vutbr.fit.layout.segm.op;

    provides cz.vutbr.fit.layout.api.ArtifactService
            with cz.vutbr.fit.layout.segm.BasicSegmProvider;

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

}
