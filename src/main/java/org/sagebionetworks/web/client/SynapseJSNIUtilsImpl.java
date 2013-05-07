package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResultJso;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
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
	public boolean isDirectUploadSupported() {
		return _isDirectUploadSupported();
	}
	
	private final static native boolean _isDirectUploadSupported() /*-{ 
		//if Safari, direct upload is not supported
		if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)
			return false;
		var xhr = new XMLHttpRequest();
		// This test is from http://blogs.msdn.com/b/ie/archive/2012/02/09/cors-for-xhr-in-ie10.aspx
		return ("withCredentials" in xhr);
	}-*/;

	@Override
	public void uploadFile(String fileFieldId, String url, XMLHttpRequest xhr, ProgressCallback callback) {
		SynapseJSNIUtilsImpl.progressCallback = callback;
		_directUploadFile(fileFieldId, url, xhr);
	}
	private final static native void _directUploadFile(String fileFieldId, String url, XMLHttpRequest xhr) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		var fileToUpload = fileToUploadElement.files[0];
		xhr.upload.onprogress = $entry(@org.sagebionetworks.web.client.SynapseJSNIUtilsImpl::updateProgress(Lcom/google/gwt/core/client/JavaScriptObject;));
  		xhr.open('PUT', url, true);
		xhr.send(fileToUpload);
	}-*/;
	
	public static void updateProgress(JavaScriptObject evt) {
		if (SynapseJSNIUtilsImpl.progressCallback != null) {
			//parse out value
			double currentProgress = _getProgress(evt);
			int percent = (int)Math.floor(currentProgress*100.0);
			String text = percent+"%";
			SynapseJSNIUtilsImpl.progressCallback.updateProgress(currentProgress, text);
		}
	}
	
	private final static native double _getProgress(JavaScriptObject evt) /*-{
		if (evt.lengthComputable) {
			return evt.loaded / evt.total;
		}
		return 0;
	}-*/;
	
	@Override
	public String getContentType(String fileFieldId) {
		return _getContentType(fileFieldId);
	}
	private final static native String _getContentType(String fileFieldId) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		return fileToUploadElement.files[0].type;
	}-*/;
	
	@Override
	public double getFileSize(String fileFieldId) {
		return _getFileSize(fileFieldId);
	}
	private final static native double _getFileSize(String fileFieldId) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		return fileToUploadElement.files[0].size;
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

}
