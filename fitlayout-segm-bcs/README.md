FitLayout/2 - Block Clustering Segmentation (BCS) Algorithm
===========================================================

(c) 2020 Radek Burget (burgetr@fit.vutbr.cz)

This is a new FitLayout implementation of the BCS algorithm as published in

```
ZELENÝ Jan, BURGET Radek and ZENDULKA Jaroslav. Box Clustering Segmentation: A New Method for Vision-based Page Preprocessing. Information Processing and Management, vol. 53, no. 3, pp. 735-750. ISSN 0306-4573. Available from: http://www.sciencedirect.com/science/article/pii/S0306457316301169
```

The code is greatly based on the original implementation available in [janzeleny/bcs](https://github.com/janzeleny/bcs). However, the original implementation is based on an older version of the CSSBox rendering engine whereas our code has been ported to use the FitLayout generic page model and its rendering backends. Therefore this implementation may provide slightly different results than the original one.
