FitLayout/2 - Generic JSON-based page description renderer
==========================================================

(c) 2022 Radek Burget (burgetr@fit.vutbr.cz)

This module provides a generic page renderer that creates a box tree from a JSON description of a page, which is
typically obtained from an external browser. This module serves as a base for the 
[fitlayout-render-puppeteer](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-render-puppeteer) and
[fitlayout-render-playwright](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-render-playwright) modules
that implement the browser invocation and obtaining the page description in different ways.
