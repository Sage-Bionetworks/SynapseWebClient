package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;

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
		$wnd.jQuery('#'+id).tooltip();
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
	
	private static native void _bindBootstrapPopover(String id) /*-{
		$wnd.jQuery('#'+id).popover();
	}-*/;

	private static DateTimeFormat smallDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm:ssaa");
	@Override
	public String convertDateToSmallString(Date toFormat) {
		return smallDateFormat.format(toFormat);
	}

	@Override
	public String getBaseProfileAttachmentUrl() {
		return GWT.getModuleBaseURL() + "profileAttachment";
	}

	@Override
	public int randomNextInt() {
		return Random.nextInt();
	}

}
