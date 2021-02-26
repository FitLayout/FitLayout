FitLayout/2 - Visual Page Segmentation (VIPS) Algorithm
=======================================================

(c) 2020 Radek Burget (burgetr@fit.vutbr.cz)

This is a new FitLayout implementation of the VIPS algorithm as published in

```
Deng Cai, Shipeng Yu, Ji-Rong Wen and Wei-Ying Ma. "VIPS: a Vision-based Page Segmentation Algorithm", Microsoft Technical Report (MSR-TR-2003-79), 2003
```

The code is in minor part based on [tpopela/vips](https://github.com/tpopela/vips_java); however, the algorithm has been rewritten from scratch in order to follow the orignal paper as close as possible.

The implementation has been validated according to the [original VIPS implementation](http://www.cad.zju.edu.cn/home/dengcai/VIPS/VIPS.html). However, the original one is provided in binary (win32) form only and it is based on an obsolete rendering engine. Moreover, despite a thorough study of the related papers, some details are not clearly described (e.g. the exact way of DoC computation). Therefore our implementation may provide slightly different results than the original one.

## Services and parameters

Artifact service ID: `FitLayout.VIPS`

Consumes: [Page](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/Page.html)

Produces: [AreaTree](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/AreaTree.html)

Options:
- `pDoC=<int>` - predefined degree of coherence (1 - 11), default 10

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#segment) or when invoking the service via the Java API or the REST API.
