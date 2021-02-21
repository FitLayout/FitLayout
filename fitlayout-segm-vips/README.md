FitLayout/2 - Visual Page Segmentation (VIPS) Algorithm
=======================================================

(c) 2020 Radek Burget (burgetr@fit.vutbr.cz)

This is a new FitLayout implementation of the VIPS algorithm as published in

```
Deng Cai, Shipeng Yu, Ji-Rong Wen and Wei-Ying Ma. "VIPS: a Vision-based Page Segmentation Algorithm", Microsoft Technical Report (MSR-TR-2003-79), 2003
```

The code is in minor part based on [tpopela/vips](https://github.com/tpopela/vips_java); however, the algorithm has been rewritten from scratch in order to follow the orignal paper as close as possible.

The implementation has been validated according to the [original VIPS implementation](http://www.cad.zju.edu.cn/home/dengcai/VIPS/VIPS.html). However, the original one is provided in binary (win32) form only and it is based on an obsolete rendering engine. Moreover, despite a thorough study of the related papers, some details are not clearly described (e.g. the exact way of DoC computation). Therefore our implementation may provide slightly different results than the original one.
