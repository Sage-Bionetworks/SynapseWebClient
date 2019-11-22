importScripts('https://cdnjs.cloudflare.com/ajax/libs/spark-md5/3.0.0/spark-md5.min.js');

onmessage = function(e) {
	console.log('Message received from main script');
	var file = e.data;
	var spark = new SparkMD5.ArrayBuffer();
	var blobSlice = file.slice || file.mozSlice || file.webkitSlice;
	chunkSize = 2097152; // read in chunks of 2MB
	chunks = Math.ceil(file.size / chunkSize);
	currentChunk = 0;
	frOnload = function (e) {
		console.log("read chunk nr", currentChunk + 1, "of", chunks);
		spark.append(e.target.result);	// append array buffer
		currentChunk++;

		if (currentChunk < chunks) {
			loadNext();
		}
		else {
			console.log("finished loading file (to calculate md5)");
			// Call instance method setMD5() on md5Callback with the final md5
			postMessage(spark.end());
		}
	};
	frOnerror = function () {
		console.warn("unable to calculate md5");
		postMessage(null);
	};

	loadNext = function () {
		var fileReader = new FileReader();
		fileReader.onload = frOnload;
		fileReader.onerror = frOnerror;

		var start = currentChunk * chunkSize,
			end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
		console.log("MD5 full file: loading next chunk: start=", start, " end=", end);
		fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
	};
	loadNext();
}
