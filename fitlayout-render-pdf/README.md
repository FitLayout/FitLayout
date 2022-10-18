FitLayout/2 - PDF Renderer
==========================

(c) 2022 Radek Burget (burgetr@fit.vutbr.cz)

This module provides a renderer that creates the Page artifacts from PDF documents. The implementation is based on [Apache PDFBox](https://pdfbox.apache.org/).

## Services and parameters

Artifact service ID: `FitLayout.PDF`

Consumes: *no input*

Produces: [Page](http://fitlayout.github.io/api/latest/cz.vutbr.fit.layout.core/cz/vutbr/fit/layout/model/Page.html)

Options:
- `url=<string>` - source document URL (use the `file://` protocol for local documents)
- `acquireImages=<boolean>` - should the resulting Page include the images present in the PDF?
- `includeScreenshot=<boolean>` - include the screenshot of the rendered page in the page model
- `startPage=<int>` - the first page to be rendered (starting with 0, default is 0)
- `endPage=<int>` - the last page to be rendered (default is 1000)
- `zoom=<float>` - zoom factor to be applied on the rendered page (default 1.0)

The options may be specified via the [command line](https://github.com/FitLayout/FitLayout/wiki/Command-line-Interface#render) or when invoking the service via the Java API or the REST API.
