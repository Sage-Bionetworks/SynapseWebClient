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

public class SynapseJSNIUtilsImpl implements SynapseJSNIUtils {

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
	
}
