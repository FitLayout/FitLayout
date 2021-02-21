FitLayout/2 - Puppeteer-based Page Renderer
===========================================

(c) 2015-2021 Radek Burget (burgetr@fit.vutbr.cz)


This module provides a page renderer that internally uses the [fitlayout-puppeteer](https://github.com/FitLayout/fitlayout-puppeteer) project as a rendering backend. The rendering itself is provided by a built-in Chromium web browser. This ensures reliable rendering of complex web pages including JavaScript support. On the other hand, additional configuration of the backend is required and launching the Chromium browser means more overhead than using the simple internal [CSSBox renderer](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-render-cssbox).

## Installation

1. Install the [fitlayout-puppeteer](https://github.com/FitLayout/fitlayout-puppeteer) backend according to its installation instructions.
2. Set the `fitlayout.puppeteer.backend` Java system property to point to your fitlayout-puppeteer installation. This may be done via the Java command line (the `-D` option) or in a `config.properties` file located in the working directory as described in [Configuration](https://github.com/FitLayout/FitLayout/wiki/Installation#configuration).

The FitLayout [docker images](https://github.com/FitLayout/docker-images) contain a configured ready-to use puppeteer renderer. No further configuration is necessary when using the docker images.
