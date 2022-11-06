/*
 * FitLayout puppetteer backend.
 * (c) 2020-2022 Radek Burget <burgetr@fit.vutbr.cz>
 * 
 * jsonld.js
 * JSON-LD metadata extraction
 */

 function extractJsonLd(document) {

	let ret = [];
	let list = document.querySelectorAll("script[type='application/ld+json']");
	for (let item of list) {
		ret.push({
			type: 'application/ld+json',
			content: item.textContent
		});
	}
	return ret;

 }
