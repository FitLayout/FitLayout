FitLayout/2 - Basic Page Segmentation
=====================================

(c) 2015-2021 Radek Burget (burgetr@fit.vutbr.cz)

This module implements the basic bottom-up page segmentation algorithm as published in 

```
BURGET Radek. Automatic Document Structure Detection for Data Integration. In: Business Information Systems. Lecture Notes in Computer Science, vol. 4439. Poznan: Springer Verlag, 2007, pp. 391-397. ISBN 978-3-540-72034-8.
```

The input of the algorithm is a *visual box tree* which is a post-processed *box tree* that can be obtained for example using the [VisualBoxTreeProvider](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-core/src/main/java/cz/vutbr/fit/layout/provider/VisualBoxTreeProvider.java). The whole process can be summarized in the following steps:

1. Visual box tree construction ([VisualBoxTreeProvider](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-core/src/main/java/cz/vutbr/fit/layout/provider/VisualBoxTreeProvider.java)) 
2. Basic visual area tree construction ([SegmentationAreaTree](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-segm-base/src/main/java/cz/vutbr/fit/layout/segm/SegmentationAreaTree.java))
3. Area tree post-processing using area tree operators. The segmentation itself is implemented by the [SuperAreaOperator](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-segm-base/src/main/java/cz/vutbr/fit/layout/segm/op/SuperAreaOperator.java).

The module also implements a number of [generally applicable area tree operators](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-segm-base/src/main/java/cz/vutbr/fit/layout/segm/op) that can be re-used for further area tree analysis such as [line detection](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-segm-base/src/main/java/cz/vutbr/fit/layout/segm/op/FindLineOperator.java) or [detection of style-consistent leaf areas](https://github.com/FitLayout/FitLayout/blob/main/fitlayout-segm-base/src/main/java/cz/vutbr/fit/layout/segm/op/HomogeneousLeafOperator.java).

## Services and parameters

Artifact service ID: `FitLayout.Grouping`

Consumes: [Page](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/Page.html)

Produces: [AreaTree](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/AreaTree.html)

Options:
- `preserveAuxAreas=<boolean>` - preserve auxiliary boxes with no visual representation

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#segment) or when invoking the service via the Java API or the REST API.
