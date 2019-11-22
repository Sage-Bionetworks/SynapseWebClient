importScripts('https://cdnjs.cloudflare.com/ajax/libs/spark-md5/3.0.0/spark-md5.min.js');

onmessage = function(e) {
	var file = e.data.file;
	var currentChunk = e.data.currentChunk;
	var chunkSize = e.data.chunkSize;
	var blobSlice = file.slice || file.mozSlice || file.webkitSlice;
	spark = new SparkMD5.ArrayBuffer();
	frOnload = function (e) {
		spark.append(e.target.result); // append array buffer
		// Call instance method setMD5() on md5Callback with the final md5
		postMessage(spark.end());
	};
	frOnerror = function () {
		console.warn("unable to calculate file part md5");
		postMessage(null);
	};
	
	loadPart = function () {
		var fileReader = new FileReader();
		fileReader.onload = frOnload;
		fileReader.onerror = frOnerror;
		var start = currentChunk * chunkSize,
		end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
	
		console.log("MD5 file part: loading chunk: start=", start, " end=", end);
		fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
	};
	loadPart();
}
