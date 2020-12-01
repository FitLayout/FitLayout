FitLayout/2 - Visual Page Segmentation (VIPS) Algorithm
=======================================================

(c) 2020 Radek Burget (burgetr@fit.vutbr.cz)

This is a new FitLayout implementation of the VIPS algorithm as published in

```
Deng Cai, Shipeng Yu, Ji-Rong Wen and Wei-Ying Ma. "VIPS: a Vision-based Page Segmentation Algorithm", Microsoft Technical Report (MSR-TR-2003-79), 2003
```

This code is partly based on [tpopela/vips](https://github.com/tpopela/vips_java); however, it has been significantly rewritten to follow the orignal paper as close as possible.

The [original VIPS implementation](http://www.cad.zju.edu.cn/home/dengcai/VIPS/VIPS.html) is provided in binary (win32) form only and it is based on an obsolete rendering engine. Moreover, despite a detailed study of the related papers, some details are not clearly described in the technical report (e.g. the exact way of DoC computation). Therefore this implementation may provide slightly different results than the original one.
