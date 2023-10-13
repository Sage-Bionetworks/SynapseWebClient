// determine cdn endpoint (staging or prod, do not use a cdn for local or dev builds)
var cdnEndpoint = '//cdn-' + location.hostname + '/'
var synapseOrgRegex = /(www|staging|tst)\.synapse\.org$/

if (!synapseOrgRegex.test(location.hostname)) {
  cdnEndpoint = '/'
}

importScripts(cdnEndpoint + 'generated/spark-md5.min.js')

onmessage = function (e) {
  console.log('Message received from main script')
  let file = e.data
  let spark = new SparkMD5.ArrayBuffer()
  let blobSlice = file.slice || file.mozSlice || file.webkitSlice
  const chunkSize = 2097152 // read in chunks of 2MB
  let chunks = Math.ceil(file.size / chunkSize)
  let currentChunk = 0
  let frOnload = function (e) {
    console.log('read chunk nr', currentChunk + 1, 'of', chunks)
    spark.append(e.target.result) // append array buffer
    currentChunk++

    if (currentChunk < chunks) {
      loadNext()
    } else {
      let md5 = spark.end()
      console.log('finished loading file (to calculate md5): ' + md5)
      // Call instance method setMD5() on md5Callback with the final md5
      postMessage(md5)
    }
  }
  let frOnerror = function () {
    console.warn('unable to calculate md5')
    postMessage(null)
  }

  let loadNext = function () {
    let fileReader = new FileReader()
    fileReader.onload = frOnload
    fileReader.onerror = frOnerror

    let start = currentChunk * chunkSize,
      end = start + chunkSize >= file.size ? file.size : start + chunkSize
    console.log(
      'MD5 full file: loading next chunk: start=',
      start,
      ' end=',
      end,
    )
    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
  }
  loadNext()
}
