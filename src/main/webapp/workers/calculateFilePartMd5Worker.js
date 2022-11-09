importScripts(
  'https://cdnjs.cloudflare.com/ajax/libs/spark-md5/3.0.0/spark-md5.min.js',
)

onmessage = function (e) {
  let file = e.data.file
  let currentChunk = e.data.currentChunk
  let chunkSize = e.data.chunkSize
  let blobSlice = file.slice || file.mozSlice || file.webkitSlice
  let spark = new SparkMD5.ArrayBuffer()
  let frOnload = function (e) {
    spark.append(e.target.result) // append array buffer
    // Call instance method setMD5() on md5Callback with the final md5
    let md5 = spark.end()
    console.log('finished loading file part (to calculate md5): ' + md5)
    postMessage(md5)
  }
  let frOnerror = function () {
    console.warn('unable to calculate file part md5')
    postMessage(null)
  }

  let loadPart = function () {
    let fileReader = new FileReader()
    fileReader.onload = frOnload
    fileReader.onerror = frOnerror
    let start = currentChunk * chunkSize,
      end = start + chunkSize >= file.size ? file.size : start + chunkSize

    console.log('MD5 file part: loading chunk: start=', start, ' end=', end)
    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
  }
  loadPart()
}
