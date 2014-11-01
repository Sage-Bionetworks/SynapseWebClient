package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderViewImpl;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResultJso;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class SynapseJSNIUtilsImpl implements SynapseJSNIUtils {
	
	private static ProgressCallback progressCallback;
	
	@Override
	public void recordPageVisit(String token) {
		_recordPageVisit(token);
	}

	private static native void _recordPageVisit(String token) /*-{
		$wnd._gaq.push(['_trackPageview', token]);
	}-*/;
	
	@Override
	public String getCurrentHistoryToken() {
		return History.getToken();
	}

	@Override
	public void bindBootstrapTooltip(String id) {
		_bindBootstrapTooltip(id);
	}

	private static native void _bindBootstrapTooltip(String id) /*-{
		$wnd.jQuery('#'+id).tooltip().tooltip('fixTitle');	//update title from data-original-title, if necessary
	}-*/;

	@Override
	public void hideBootstrapTooltip(String id) {
		_hideBootstrapTooltip(id);
	}

	private static native void _hideBootstrapTooltip(String id) /*-{
		$wnd.jQuery('#'+id).tooltip('hide');
	}-*/;
	
	@Override
	public void bindBootstrapPopover(String id) {
		_bindBootstrapPopover(id);
	}
	
	@Override
	public void highlightCodeBlocks() {
		_highlightCodeBlocks();
	}
	
	public static native void _highlightCodeBlocks() /*-{
	  $wnd.jQuery('code').each(function(i, e) {$wnd.hljs.highlightBlock(e)});
	}-*/;
	
	@Override
	public void tablesorter(String id) {
		_tablesorter(id);
	}
	
	private static native void _tablesorter(String id) /*-{
		$wnd.jQuery('#'+id).tablesorter();
	}-*/;
	
	private static native void _bindBootstrapPopover(String id) /*-{
		$wnd.jQuery('#'+id).popover();
	}-*/;

	private static DateTimeFormat smallDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm:ssaa");
	@Override
	public String convertDateToSmallString(Date toFormat) {
		return smallDateFormat.format(toFormat);
	}

	@Override
	public String getBaseFileHandleUrl() {
		return GWT.getModuleBaseURL()+"filehandle";
	}
	
	@Override
	public String getBaseProfileAttachmentUrl() {
		return GWT.getModuleBaseURL() + "profileAttachment";
	}

	@Override
	public int randomNextInt() {
		return Random.nextInt();
	}

	@Override
	public String getLocationPath() {
		return Location.getPath();
	}

	@Override
	public String getLocationQueryString() {
		return Location.getQueryString();
	}

	@Override
	public LayoutResult nChartlayout(NChartLayersArray layers,
			NChartCharacters characters) {		
		return _nChartlayout(layers, characters);
	}

	private final static native LayoutResultJso _nChartlayout(NChartLayersArray layers, NChartCharacters characters) /*-{	        
	    var debug = {'features': ['nodes'], 'wireframe': true};
		var conf = {'group_styles': {'pov': {'stroke-width': 3}},
	        'debug': debug};	        
		var chart = new $wnd.NChart(characters, layers, conf).calc().plot();
			
		// convert graph into LayoutResult
		var layoutResult = {}; 
		var ncGraph = chart.graph;
		for(var i=0; i<ncGraph.layers.length; i++) {		
			var ncLayer = ncGraph.layers[i];
			for(var j=0; j<ncLayer.nodes.length; j++) {
				var ncNode = ncLayer.nodes[j];
				var provGraphNodeId = ncNode.event;
				var xypoint = { 'x':ncNode.x, 'y':ncNode.y };
				if(!(provGraphNodeId in layoutResult)) { 
					layoutResult[provGraphNodeId] = [];
				}
				layoutResult[provGraphNodeId].push(xypoint);				
			}
		}		
		return layoutResult;
	}-*/;

	@Override
	public void setPageTitle(String newTitle) {
	    if (Document.get() != null) {
	        Document.get().setTitle (newTitle);
	    }
	}
	
	@Override
	public void setPageDescription(String newDescription) {
		if (Document.get() != null) {
			NodeList<com.google.gwt.dom.client.Element> tags = Document.get().getElementsByTagName("meta");
		    for (int i = 0; i < tags.getLength(); i++) {
		        MetaElement metaTag = ((MetaElement) tags.getItem(i));
		        if (metaTag.getName().equals("description")) {
		            metaTag.setContent(newDescription);	//doesn't seem to work
		            break;
		        }
		    }
		}
	}
	
	@Override
	public void uploadFileChunk(String contentType, int index, String fileFieldId, Long startByte, Long endByte, String url, XMLHttpRequest xhr, ProgressCallback callback) {
		SynapseJSNIUtilsImpl.progressCallback = callback;
		_directUploadFile(contentType, index, fileFieldId, startByte, endByte, url, xhr);
	}
	
	private final static native void _directUploadFile(String contentType, int index, String fileFieldId, Long startByte, Long endByte, String url, XMLHttpRequest xhr) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		var fileToUpload = fileToUploadElement.files[index];
		var start = parseInt(startByte) || 0;
		var end = parseInt(endByte) || fileToUpload.size - 1;
		var fileSliceToUpload;
		//in versions later than Firefox 13 and Chrome 21, Blob.slice() is not prefixed (and the vendor prefixed methods are deprecated)
		if (fileToUpload.slice) {
        	fileSliceToUpload = fileToUpload.slice(start, end+1, contentType);
	    }else if (fileToUpload.mozSlice) {
        	fileSliceToUpload = fileToUpload.mozSlice(start, end+1, contentType);
	    } else if (fileToUpload.webkitSlice) {
	        fileSliceToUpload = fileToUpload.webkitSlice(start, end+1, contentType);
	    } else {
	        throw new Error("Unable to slice file.");
	    }
		xhr.upload.onprogress = $entry(@org.sagebionetworks.web.client.SynapseJSNIUtilsImpl::updateProgress(Lcom/google/gwt/core/client/JavaScriptObject;));
  		xhr.open('PUT', url, true);
  		if(contentType) {
  			xhr.setRequestHeader('Content-type', contentType);
  		}
  		xhr.send(fileSliceToUpload);
	}-*/;
	
	
	public static void updateProgress(JavaScriptObject evt) {
		if (SynapseJSNIUtilsImpl.progressCallback != null) {
			//parse out value
			double currentProgress = _getProgress(evt);
			SynapseJSNIUtilsImpl.progressCallback.updateProgress(currentProgress);
		}
	}
	
	private final static native double _getProgress(JavaScriptObject evt) /*-{
		if (evt.lengthComputable) {
			return evt.loaded / evt.total;
		}
		return 0;
	}-*/;
	
	@Override
	public String getContentType(String fileFieldId, int index) {
		return _getContentType(fileFieldId, index);
	}
	private final static native String _getContentType(String fileFieldId, int index) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		return fileToUploadElement.files[index].type;
	}-*/;
	
	@Override
	public double getFileSize(String fileFieldId, int index) {
		return _getFileSize(fileFieldId, index);
	}
	private final static native double _getFileSize(String fileFieldId, int index) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		var fileSize = ('files' in fileToUploadElement) ? fileToUploadElement.files[index].size : 0;
		return fileSize;
	}-*/;
	
	@Override
	public String[] getMultipleUploadFileNames(String fileFieldId) {
		String unSplitNames = _getFilesSelected(fileFieldId);
		if (unSplitNames.equals(""))
			return null;
		return unSplitNames.split(";");
	}
	
	private static native String _getFilesSelected(String fileFieldId) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
	    var out = "";
	
	    for (i = 0; i < fileToUploadElement.files.length; i++) {
	        var file = fileToUploadElement.files[i];
	        out += file.name + ';';
	    }
	    return out;
	}-*/;
	
	/**
	 * Using SparkMD5 (https://github.com/satazor/SparkMD5) to (progressively by slicing the file) calculate the md5.
	 */
	@Override
	public void getFileMd5(String fileFieldId, int index, MD5Callback md5Callback) {
		_getFileMd5(fileFieldId, index, md5Callback);
	}
	private final static native void _getFileMd5(String fileFieldId, int index, MD5Callback md5Callback) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		var file = fileToUploadElement.files[index];
		var blobSlice = file.slice || file.mozSlice || file.webkitSlice;
		chunkSize = 2097152; // read in chunks of 2MB
        chunks = Math.ceil(file.size / chunkSize);
        currentChunk = 0;
        spark = new $wnd.SparkMD5.ArrayBuffer();
        $wnd.frOnload = function(e) {
            console.log("read chunk nr", currentChunk + 1, "of", chunks);
            spark.append(e.target.result);                 // append array buffer
            currentChunk++;

            if (currentChunk < chunks) {
                $wnd.loadNext();
            }
            else {
               console.log("finished loading file (to calculate md5)");
               // Call instance method setMD5() on md5Callback with the final md5
    			md5Callback.@org.sagebionetworks.web.client.callback.MD5Callback::setMD5(Ljava/lang/String;)(spark.end());
            }
        };
        $wnd.frOnerror = function () {
        	console.warn("unable to calculate md5");
            md5Callback.@org.sagebionetworks.web.client.callback.MD5Callback::setMD5(Ljava/lang/String;)(null);
        };
        
        $wnd.loadNext = function() { 
            var fileReader = new FileReader();
	        fileReader.onload = $wnd.frOnload;
	        fileReader.onerror = $wnd.frOnerror;
	
	        var start = currentChunk * chunkSize,
	            end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
	
	        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
		};
       $wnd.loadNext();
	}-*/;

	/**
	 * Get the url to a local file blob.
	 */
	@Override
	public String getFileUrl(String fileFieldId) {
		return _getFileUrl(fileFieldId);
	}
	private final static native String _getFileUrl(String fileFieldId) /*-{
		try {
			var fileToUploadElement = $doc.getElementById(fileFieldId);
			var file = fileToUploadElement.files[0];
			return URL.createObjectURL(file);
		}catch(err) {
			return null;
		}
	}-*/;

	@Override
	public void uploadUrlToGenomeSpace(String url) {
		_uploadUrlToGenomeSpace(url, null);
	}

	@Override
	public void uploadUrlToGenomeSpace(String url, String filename) {
		_uploadUrlToGenomeSpace(url, filename);		
	}

	private final static native void _uploadUrlToGenomeSpace(String url, String fileName) /*-{
		var gsUploadUrl = "https://gsui.genomespace.org/jsui/upload/loadUrlToGenomespace.html?uploadUrl=";
		var dest = $wnd.encodeURIComponent(url);
		gsUploadUrl += dest;
		if(fileName != null) {
			gsUploadUrl += "&fileName=" + fileName;
		}
		var newWin = $wnd.open(gsUploadUrl, "GenomeSpace Upload", "height=340px,width=550px");
		newWin.focus();
		newWin.setCallbackOnGSUploadComplete = function(savePath) {
			alert('outer Saved to GenomeSpace as ' + savePath);
		}
		newWin.setCallbackOnGSUploadError = function(savePath) {
			alert('outer ERROR saving to GenomeSpace as ' + savePath);
		}
	}-*/;

	@Override
	public void consoleLog(String message) {
		_consoleLog(message);
	}

	public final static native void _consoleLog(String message) /*-{
		console.log(message);
	}-*/;

	@Override
	public void consoleError(String message) {
		_consoleError(message);
	}

	public final static native void _consoleError(String message) /*-{
		console.error(message);
	}-*/;
	
	@Override
	public void processWithMathJax(Element element) {
		_processWithMathJax(element);		
	}

	private final static native void _processWithMathJax(Element element) /*-{
		$wnd.layoutMath(element);
	}-*/;

	@Override
	public void loadCss(final String url, final Callback<Void, Exception> callback) {
		final LinkElement link = Document.get().createLinkElement();
		link.setRel("stylesheet");
		link.setHref(url);
		_nativeAttachToHead(link);
		
		// fall back timer
		final Timer t = new Timer() {
			@Override
			public void run() {
				callback.onSuccess(null);
			}
		};
		
		Command loadedCommand = new Command() {			
			@Override
			public void execute() {
				callback.onSuccess(null);
				t.cancel();
			}
		};
		
		_addCssLoadHandler(url, loadedCommand);		
		t.schedule(5000); // failsafe: after 5 seconds assume loaded
	}
	
	/**
	 * Attach element to head
	 */
	protected static native void _nativeAttachToHead(JavaScriptObject scriptElement) /*-{
	    $doc.getElementsByTagName("head")[0].appendChild(scriptElement);
	}-*/;


	/**
	 * provides a callback mechanism for when CSS resources that have been added to the dom are fully loaded
	 * @param cssUrl
	 * @param callback
	 */
	private static native void _addCssLoadHandler(String cssUrl, Command command) /*-{
		// Use Image load error callback to detect loading as no reliable/cross-browser callback exists for Link element
		var img = $doc.createElement('img');		
		img.onerror = function() {			
			command.@com.google.gwt.user.client.Command::execute()();
		}
		img.src = cssUrl;
	}-*/;
}
