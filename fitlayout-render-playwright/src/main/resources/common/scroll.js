async function scrollDown(maxIterations) {
        
        let totalHeight = 0;
        await new Promise((resolve, reject) => {
			let iteration = 0;
			const distance = window.innerHeight / 2; // div 2 is for scrolling slower and let everything load
            var timer = setInterval(() => {
                const scrollHeight = document.body.scrollHeight;
                window.scrollBy({top: distance, left: 0, behavior: 'auto'});
				totalHeight += distance;
				iteration++;

                if (totalHeight >= scrollHeight || iteration > maxIterations) {
					totalHeight = scrollHeight; // for returning the maximal height in all cases
					window.scrollTo({top: 0, left: 0, behavior: 'auto'});
                    clearInterval(timer);
                    resolve();
                }
            }, 100);
		});
		return totalHeight;
}
