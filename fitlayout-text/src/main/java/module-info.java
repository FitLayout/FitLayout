module cz.vutbr.fit.layout.text
{
    requires java.xml;
    requires java.desktop;
    requires org.slf4j;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires stanford.corenlp;
    
    exports cz.vutbr.fit.layout.text.op;
    exports cz.vutbr.fit.layout.text.tag;
    exports cz.vutbr.fit.layout.text.taggers;
    
    provides cz.vutbr.fit.layout.api.AreaTreeOperator
        with cz.vutbr.fit.layout.text.op.TagEntitiesOperator;

}
