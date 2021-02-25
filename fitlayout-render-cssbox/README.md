FitLayout/2 - CSSBox-based Page Renderer
========================================

(c) 2015-2021 Radek Burget (burgetr@fit.vutbr.cz)


This module provides a simple page renderer with HTML 5 + CSS 3 and PDF support. It is based on the built-in [CSSBox](http://cssbox.sf.net) rendering engine. It has no external dependencies and it runs out-of-the-box. It is suitable for a quick rendering of simple web pages that do not require starting a full-featured browser. On the other hand, it provides no JavaScript support.

For HTML pages with a complex CSS layout or pages that depend on JavaScript, the full-featured [puppeteer renderer](https://github.com/FitLayout/FitLayout/tree/main/fitlayout-render-puppeteer) should be used.

## Services and parameters

Artifact service ID: `FitLayout.CSSBox`

Consumes: *no input*

Produces: [Page]()

Options:
- `url=<string>` - source page URL
- `width=<int>` - viewport width used for rendering
- `height=<int>` - viewport height used for rendering
- `acquireImages=<boolean>` - download images referenced in HTML?
- `includeScreenshot=<boolean>` - include the screenshot of the rendered page in the page representation

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#render) or when invoking the service via the Java API or the REST API.
