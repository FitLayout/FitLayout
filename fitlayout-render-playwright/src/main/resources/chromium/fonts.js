/*
 * fitlayout-puppeteer -- Puppeteer-based web page renderer for FitLayout
 * (c) Radek Burget 2020-2021
 *
 * fonts.js
 * Font handling functions.
 */

/**
 * Tries to disable CSS-linked fonts.
 */
function disableCSSFonts() {
	
	for (i=0; i < document.styleSheets.length; i++) { 
		//console.log(document.styleSheets[i].href);
		let ss = document.styleSheets[i];
		if (typeof ss.href === 'string') {
			if (ss.href.indexOf('fonts.googleapis.com') !== -1) {
				ss.disabled = true;
			}
		} 
	}
}
