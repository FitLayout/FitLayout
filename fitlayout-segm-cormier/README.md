FitLayout/2 - Visual Page Segmentation (VIPS) Algorithm
=======================================================

(c) 2023 [František Maštera](https://github.com/MightyW0lf)

This is a new FitLayout implementation of the Improved Vision-Based Web Page Segmentation Algorithm algorithm as published in

```
Cormier, M., Mann, R., Moffatt, K. a Cohen, R. Towards an Improved
Vision-Based Web Page Segmentation Algorithm. In: 2017 14th Conference on
Computer and Robot Vision (CRV). IEEE, May 2017, pp. 345–352. DOI:
10.1109/CRV.2017.38. ISBN 978-1-5386-2818-8.
```

The implementation has been validated according to the [original Python implementation](https://github.com/webis-de/ecir21-an-empirical-comparison-of-web-page-segmentation-algorithms#cormier-et-al). 

## Services and parameters

Artifact service ID: `FitLayout.Cormier`

Consumes: [Page](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/Page.html)

Produces: [AreaTree](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/AreaTree.html)

Options:
- `halfWindowWidth=<int>` - Controls the size of the neighborhood (in number of pixels from the center pixel). Higher values dramatically impact performance (0 - 1000)
- `standardDeviation=<float>` - Standard deviation for the normal distribution used in kernel density estimation for determining the probability of an edge in each pixel (0.0 - 1000.0)
- `priorEdgeProbability=<float>` - Prior probability of an edge in each pixel (0.0 - 1.0)
- `pyramidLevels=<int>` - Number of levels for the Gaussian pyramid used for the multiscale edge detection. Set to 1 to effectively turn off the multiscale edge detection (1 - 10)
- `maxLineLength=<int>` - Maximum length of a segmentation line (in pixels) before the algorithm splits it in half and processes each half separately (1, 1000)
- `edgeProbabilityThreshold=<float>` - Minimum probability of an edge in each pixel required in order for it to be considered significant (0.0 - 1.0)
- `monteCarloTrials=<int>` - Number of Monte Carlo trials for determining the probability that a line is semantically significant (1 - 5000)
- `minSegmentLength=<int>` - Minimum length of any side of each segment (0 - 1000)
- `signLineProbThreshold=<float>` - Minimum probability that a line is significant for it to be used for segmentation (0.0 - 1.0)

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#segment) or when invoking the service via the Java API or the REST API.
