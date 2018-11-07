package org.sagebionetworks.web.client;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResultJso;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class SynapseJSNIUtilsImpl implements SynapseJSNIUtils {
	
	private static ProgressCallback progressCallback;

	@Override
	public void setAnalyticsUserId(String userID) {
		_setAnalyticsUserId(userID);
	}
	private static native void _setAnalyticsUserId(String userID) /*-{
		try {
			$wnd.ga('set', 'userId', userID);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	@Override
	public void recordPageVisit(String token) {
		_recordPageVisit(token);
	}

	private static native void _recordPageVisit(String token) /*-{
		try {
			$wnd.ga('set', 'page', '/#'+token);
			$wnd.ga('send', 'pageview');
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void sendAnalyticsEvent(String eventCategory, String eventAction, String eventLabelValue) {
		_sendAnalyticsEvent(eventCategory, eventAction, eventLabelValue);
	}
	@Override
	public void sendAnalyticsEvent(String eventCategory, String eventAction) {
		_sendAnalyticsEvent(eventCategory, eventAction, getCurrentURL());
	}

	private static native void _sendAnalyticsEvent(String eventCategoryValue, String eventActionValue, String eventLabelValue) /*-{
		try {
			$wnd.ga('send', 
			{
			  hitType: 'event',
			  eventCategory: eventCategoryValue,
			  eventAction: eventActionValue,
			  eventLabel: eventLabelValue,
			  fieldsObject: { nonInteraction: true}
			});
		} catch (err) {
			console.error(err);
		}
	}-*/;

	
	@Override
	public String getCurrentHistoryToken() {
		return History.getToken();
	}
	
	@Override
	public void highlightCodeBlocks() {
		_highlightCodeBlocks();
	}
	
	public static native void _highlightCodeBlocks() /*-{
		try {
			$wnd.jQuery('pre code').each(function(i, e) {$wnd.hljs.highlightBlock(e)});
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void loadSummaryDetailsShim() {
		_loadSummaryDetailsShim();
	}
	
	public static native void _loadSummaryDetailsShim() /*-{
		try {
			$wnd.jQuery('summary').each(function(i, e) {
				$wnd.details_shim(e)
				});
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void loadTableSorters() {
		_tablesorter();
	}
	
	private static native void _tablesorter() /*-{
		try {
			$wnd.jQuery('table.markdowntable').tablesorter();
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public String getBaseFileHandleUrl() {
		return GWTWrapperImpl.getRealGWTModuleBaseURL()+"filehandle";
	}
	
	@Override
	public String getFileHandleAssociationUrl(String objectId, FileHandleAssociateType objectType, String fileHandleId) {
		return GWTWrapperImpl.getRealGWTModuleBaseURL() + WebConstants.FILE_HANDLE_ASSOCIATION_SERVLET + "?" + 
				WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY + "=" + objectId + "&" +
				WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY + "=" + objectType.toString() + "&" + 
				WebConstants.FILE_HANDLE_ID_PARAM_KEY + "=" + fileHandleId;
	}

	/**
	 * Create the url to the raw file handle id (must be the owner to access)
	 * @param rawFileHandleId
	 * @return
	 */
	@Override
	public String getRawFileHandleUrl(String fileHandleId) {
		return GWTWrapperImpl.getRealGWTModuleBaseURL() + WebConstants.FILE_HANDLE_ASSOCIATION_SERVLET + "?" + 
				WebConstants.FILE_HANDLE_ID_PARAM_KEY + "=" + fileHandleId;
	}

	@Override
	public String getSessionCookieUrl() {
		return GWTWrapperImpl.getRealGWTModuleBaseURL() + WebConstants.SESSION_COOKIE_SERVLET;
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
		var layoutResult = {};
		try {
		    var debug = {'features': ['nodes'], 'wireframe': true};
			var conf = {'group_styles': {'pov': {'stroke-width': 3}},
		        'debug': debug};	        
			var chart = new $wnd.NChart(characters, layers, conf).calc().plot();
				
			// convert graph into LayoutResult
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
		} catch (err) {
			console.error(err);
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
	public void uploadFileChunk(String contentType, JavaScriptObject blob, Long startByte, Long endByte, String url, XMLHttpRequest xhr, ProgressCallback callback) {
		SynapseJSNIUtilsImpl.progressCallback = callback;
		_directUploadBlob(contentType, blob, startByte, endByte, url, xhr);
	}
	
	@Override
	public JavaScriptObject getFileList(String fileFieldId) {
		return _getFileList(fileFieldId);
	}
	
	private final static native JavaScriptObject _getFileList(String fileFieldId) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		if (fileToUploadElement && 'files' in fileToUploadElement) {
			return fileToUploadElement.files;
		}
		return null;
	}-*/;
	
	@Override
	public JavaScriptObject getFileBlob(int index, JavaScriptObject fileList) {
		return _getFileBlob(index, fileList);
	}
	
	private final static native JavaScriptObject _getFileBlob(int index, JavaScriptObject fileList) /*-{
		return fileList[index];
	}-*/;
	
	private final static native void _directUploadBlob(String contentType, JavaScriptObject fileToUpload, Long startByte, Long endByte, String url, XMLHttpRequest xhr) /*-{
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
  		//explicitly set content type
  		xhr.setRequestHeader('Content-type', contentType);
  		xhr.send(fileSliceToUpload);
	}-*/;
	
	
	public static void updateProgress(JavaScriptObject evt) {
		if (SynapseJSNIUtilsImpl.progressCallback != null) {
			SynapseJSNIUtilsImpl.progressCallback.updateProgress(_getLoaded(evt), _getTotal(evt));
		}
	}
	
	private final static native double _getLoaded(JavaScriptObject evt) /*-{
		if (evt.lengthComputable) {
			return evt.loaded;
		}
		return 0;
	}-*/;
	
	private final static native double _getTotal(JavaScriptObject evt) /*-{
		if (evt.lengthComputable) {
			return evt.total;
		}
		return 0;
	}-*/;
	
	@Override
	public String getContentType(JavaScriptObject fileList, int index) {
		return _getContentType(fileList, index);
	}
	private final static native String _getContentType(JavaScriptObject fileList, int index) /*-{
		return fileList[index].type;
	}-*/;
	
	@Override
	public String getWebkitRelativePath(JavaScriptObject fileList, int index) {
		return _getWebkitRelativePath(fileList, index);
	}
	
	private final static native String _getWebkitRelativePath(JavaScriptObject fileList, int index) /*-{
		return fileList[index].webkitRelativePath;
	}-*/;
	
	@Override
	public double getFileSize(JavaScriptObject blob) {
		return _getFileSize(blob);
	}
	private final static native double _getFileSize(JavaScriptObject blob) /*-{
		return blob.size;
	}-*/;
	
	@Override
	public String[] getMultipleUploadFileNames(JavaScriptObject fileList) {
		String unSplitNames = _getFilesSelected(fileList);
		if (unSplitNames.equals(""))
			return null;
		return unSplitNames.split(";");
	}
	
	private static native String _getFilesSelected(JavaScriptObject fileList) /*-{
		var out = "";
	    for (i = 0; i < fileList.length; i++) {
	        var file =fileList[i];
	        out += file.name + ';';
	    }
	    return out;
	}-*/;
	
	public boolean isElementExists(String elementId) {
		return Document.get().getElementById(elementId) != null;
	};
	
	@Override
	public Element getElementById(String elementId) {
		return Document.get().getElementById(elementId);
	};
	
	/**
	 * Using SparkMD5 (https://github.com/satazor/SparkMD5) to (progressively by slicing the file) calculate the md5.
	 */
	@Override
	public void getFileMd5(JavaScriptObject blob, MD5Callback md5Callback) {
		_getFileMd5(blob, md5Callback);
	}
	private final static native void _getFileMd5(JavaScriptObject file, MD5Callback md5Callback) /*-{
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
			console.log("MD5 full file: loading next chunk: start=", start, " end=", end);
	        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
		};
       $wnd.loadNext();
	}-*/;

	/**
	 * Using SparkMD5 (https://github.com/satazor/SparkMD5) to calculate the md5 of part of a file.
	 */
	@Override
	public void getFilePartMd5(JavaScriptObject blob, int currentChunk, Long chunkSize, MD5Callback md5Callback) {
		_getFilePartMd5(blob, currentChunk, chunkSize.doubleValue(), md5Callback);
	}
	private final static native void _getFilePartMd5(JavaScriptObject file, int currentChunk, double chunkSize, MD5Callback md5Callback) /*-{
		var blobSlice = file.slice || file.mozSlice || file.webkitSlice;
		spark = new $wnd.SparkMD5.ArrayBuffer();
        $wnd.frOnload = function(e) {
            spark.append(e.target.result); // append array buffer
           // Call instance method setMD5() on md5Callback with the final md5
			md5Callback.@org.sagebionetworks.web.client.callback.MD5Callback::setMD5(Ljava/lang/String;)(spark.end());
        };
        $wnd.frOnerror = function () {
        	console.warn("unable to calculate file part md5");
            md5Callback.@org.sagebionetworks.web.client.callback.MD5Callback::setMD5(Ljava/lang/String;)(null);
        };
        
        $wnd.loadPart = function() { 
            var fileReader = new FileReader();
	        fileReader.onload = $wnd.frOnload;
	        fileReader.onerror = $wnd.frOnerror;
			var start = currentChunk * chunkSize,
	            end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
	        
	        console.log("MD5 file part: loading chunk: start=", start, " end=", end);
	        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
		};
       $wnd.loadPart();
	}-*/;
	
	@Override
	public boolean isFileAPISupported() {
		return _isFileAPISupported();
	}
	
	private final static native boolean _isFileAPISupported() /*-{
		return ($wnd.File && $wnd.FileReader && $wnd.FileList && $wnd.Blob);
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
	@Override
	public void consoleError(Throwable t) {
		_consoleError(t);
	}

	public final static native void _consoleError(Object ob) /*-{
		console.error(ob);
	}-*/;
	
	@Override
	public void processWithMathJax(Element element) {
		_processWithMathJax(element);		
	}

	private final static native void _processWithMathJax(Element element) /*-{
		try {
			$wnd.layoutMath(element);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	public final static native void _scrollIntoView(Element el) /*-{
		try {
			el.scrollIntoView({behavior: 'smooth'});
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void loadCss(final String url) {
		final LinkElement link = Document.get().createLinkElement();
		link.setRel("stylesheet");
		link.setHref(url);
		_nativeAttachToHead(link);
	}
	
	/**
	 * Attach element to head
	 */
	protected static native void _nativeAttachToHead(JavaScriptObject scriptElement) /*-{
	    $doc.getElementsByTagName("head")[0].appendChild(scriptElement);
	}-*/;
	
	@Override
	public void initOnPopStateHandler() {
		_initOnPopStateHandler();
	}

	private static native void _initOnPopStateHandler()/*-{
		// reload the page on pop state
		//we set the source property of the state if we used pushState or replaceState
		$wnd.addEventListener("popstate", function(event) {
			var stateObj = event.state;
			if (typeof stateObj !== "undefined" && stateObj !== null && typeof stateObj.source !== "undefined"){
				$wnd.location.reload(false);
			}
		});
	}-*/;
	
	@Override
	public boolean elementSupportsAttribute(Element el, String attribute) {
		return _elementSupportsAttribute(el.getTagName(), attribute);
	}
	
	private final static native boolean _elementSupportsAttribute(String tagName, String attribute) /*-{
	    return attribute in $doc.createElement(tagName);
	}-*/;
	
	boolean isFilterXssInitialized = false;
	
	@Override
	public String sanitizeHtml(String html) {
		if (!isFilterXssInitialized) {
			//init
			isFilterXssInitialized = initFilterXss();
		}
		return _sanitizeHtml(html);
	}

	private final static native boolean initFilterXss() /*-{
		try {
			var options = {
				whiteList: {
				    a:      ['target', 'href', 'title'],
				    abbr:   ['title'],
				    address: [],
				    area:   ['shape', 'coords', 'href', 'alt'],
				    article: [],
				    aside:  [],
				    audio:  ['autoplay', 'controls', 'loop', 'preload', 'src'],
				    b:      [],
				    bdi:    ['dir'],
				    bdo:    ['dir'],
				    big:    [],
				    blockquote: ['cite'],
				    body:   [],
				    br:     [],
				    caption: [],
				    center: [],
				    cite:   [],
				    code:   [],
				    col:    ['align', 'valign', 'span', 'width'],
				    colgroup: ['align', 'valign', 'span', 'width'],
				    dd:     [],
				    del:    ['datetime'],
				    details: ['open'],
				    div:    ['class'],
				    dl:     [],
				    dt:     [],
				    em:     [],
				    font:   ['color', 'size', 'face'],
				    footer: [],
				    h1:     ['toc'],
				    h2:     ['toc'],
				    h3:     ['toc'],
				    h4:     ['toc'],
				    h5:     ['toc'],
				    h6:     ['toc'],
				    head:   [],
				    header: [],
				    hr:     [],
				    html:   [],
				    i:      [],
				    img:    ['src', 'alt', 'title', 'width', 'height'],
				    ins:    ['datetime'],
				    li:     [],
				    mark:   [],
				    nav:    [],
				    noscript: [],
				    ol:     [],
				    p:      [],
				    pre:    [],
				    s:      [],
				    section:[],
				    small:  [],
				    span:   ['data-widgetparams', 'class', 'id'],
				    sub:    [],
				    summary: [],
				    sup:    [],
				    strong: [],
				    table:  ['width', 'border', 'align', 'valign', 'class'],
				    tbody:  ['align', 'valign'],
				    td:     ['width', 'rowspan', 'colspan', 'align', 'valign'],
				    tfoot:  ['align', 'valign'],
				    th:     ['width', 'rowspan', 'colspan', 'align', 'valign', 'class'],
				    thead:  ['align', 'valign'],
				    tr:     ['rowspan', 'align', 'valign'],
				    tt:     [],
				    u:      [],
				    ul:     [],
				    video:  ['autoplay', 'controls', 'loop', 'preload', 'src', 'height', 'width']
				},
				stripIgnoreTagBody: true,  // filter out all tags not in the whitelist
				allowCommentTag: false,
				css: false,
				onIgnoreTag: function (tag, html, options) {
					if (tag === '!doctype') {
				      // do not filter doctype
				      return html;
				    }
				},
				safeAttrValue: function (tag, name, value) {
					// returning nothing removes the value
					if (tag === 'img' && name === 'src') {
						if (value && (value.startsWith('data:image/') || value.startsWith('http'))) {
							return value;
						}
					} else {
						return value; 
					}
				}
			};
			$wnd.xss = new $wnd.filterXSS.FilterXSS(options);
			return true;
		} catch (err) {
			console.error(err);
			return false;
		}
	}-*/;
	
	private final static native String _sanitizeHtml(String html) /*-{
		try {
			return $wnd.xss.process(html);
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public String getCurrentURL() {
		return Location.getHref();
	}
	
	@Override
	public String getCurrentHostName() {
		return Location.getHostName();
	}
	
	@Override
	public String getProtocol(String url) {
		return _getProtocol(url);
	}
	private final static native String _getProtocol(String url) /*-{
		var parser = $doc.createElement('a');
		parser.href = url;
		var v = parser.protocol; // for example, "https:"
		parser = null; 
		return v;
	}-*/;
	
	@Override
	public String getHost(String url) {
		return _getHost(url);
	}
	
	private final static native String _getHost(String url) /*-{
		var parser = $doc.createElement('a');
		parser.href = url;
		var v = parser.host;     // for example, "test.com:8080"
		parser = null; 
		return v;
	}-*/;
	
	@Override
	public String getHostname(String url) {
		return _getHostname(url);
	}
	
	private final static native String _getHostname(String url) /*-{
		var parser = $doc.createElement('a');
		parser.href = url;
		var v = parser.hostname; // for example, "test.com"
		parser = null; 
		return v;
	}-*/;
	
	@Override
	public String getPort(String url) {
		return _getPort(url);
	}
	private final static native String _getPort(String url) /*-{
		var parser = $doc.createElement('a');
		parser.href = url;
		var v = parser.port;     // for example, "8080"
		parser = null; 
		return v;
	}-*/;
	
	@Override
	public String getPathname(String url) {
		return _getPathname(url);
	}
	
	private final static native String _getPathname(String url) /*-{
		var parser = $doc.createElement('a');
		parser.href = url;
		var v = parser.pathname; // for example, "/resources/images/" 
		parser = null; 
		return v;
	}-*/;
	
	@Override
	public void copyToClipboard() {
		try {
			_copyToClipboard();
			Notify.hideAll();
			NotifySettings settings = DisplayUtils.getDefaultSettings();
			settings.setType(NotifyType.INFO);
			Notify.notify("", "Copied to clipboard", IconType.CHECK_CIRCLE, settings);
		} catch (Throwable t) {
			consoleError(t.getMessage());
		}
	}

	private final static native String _copyToClipboard() /*-{
		$doc.execCommand('copy');
	}-*/;
	
	@Override
	public String getCdnEndpoint() {
		return _getCdnEndpoint();
	}

	private final static native String _getCdnEndpoint() /*-{
		// initialized in Portal.html
		return $wnd.cdnEndpoint;
	}-*/;
	
	public final static native void _unmountComponentAtNode(Element el) /*-{
		return $wnd.ReactDOM.unmountComponentAtNode(el);
	}-*/;
}
