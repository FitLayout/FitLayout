FitLayout/2 - Playwright-based Page Renderer
============================================

(c) 2015-2022 Radek Burget (burgetr@fit.vutbr.cz)


This module provides a page renderer that uses a Chrome browser in the background, which is remotely controlled using the Playwright tool.
This renderer provides the same rendering results as the [Puppeteer renderer](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-render-puppeteer) with no installation required (the browsers are installed automatically when used for the first time). On the other hand, since
the browser is controlled directly from Java, its configuration options are limited in comparison with the separate Node.js backend provided
by puppeteer.


## Installation

1. Install the [fitlayout-puppeteer](https://github.com/FitLayout/fitlayout-puppeteer) backend according to its installation instructions.
2. Set the `fitlayout.puppeteer.backend` Java system property to point to your fitlayout-puppeteer installation. This may be done via the Java command line (the `-D` option) or in a `config.properties` file located in the working directory as described in [Configuration](https://github.com/FitLayout/FitLayout/wiki/Installation#configuration).

The FitLayout [docker images](https://github.com/FitLayout/docker-images) contain a configured ready-to use puppeteer renderer. No further configuration is necessary when using the docker images.

## Services and parameters

Artifact service ID: `FitLayout.Playwright`

Consumes: *no input*

Produces: [Page](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/Page.html)

Options:
- `url=<string>` - source page URL
- `width=<int>` - viewport width used for rendering
- `height=<int>` - viewport height used for rendering
- `persist=<int>` - rendering *persistence* for complex dynamic pages (see below)
- `acquireImages=<boolean>` - download images referenced in HTML?
- `includeScreenshot=<boolean>` - include the screenshot of the rendered page in the page representation

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#render) or when invoking the service via the Java API or the REST API.

The `persist` value defines how much effort the underlying browser will make to download the complete contents of the page. The greater the value is, the more time the browser spends on rendering and the more complete the result is. An approximate explanation of the values is the following:

- `persistence = 0` (quick) - waits until the entire DOM tree is created (the `DOMContentLoaded` browser event), timeout 10 seconds.
- `persistence = 1` (standard) - waits until the browser loads the entire page (the `load` browser event), timeout 15 seconds.
- `persistence = 2` (longer) - waits until there are no more than 2 network connections for at least 500 ms, total timeout 15 seconds.
- `persistence = 3` (get as much as possible) - waits until there are no network connections for at least 500 ms, total timeout 50 seconds.

