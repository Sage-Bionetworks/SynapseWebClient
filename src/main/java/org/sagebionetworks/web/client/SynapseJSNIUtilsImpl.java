package org.sagebionetworks.web.client;

import com.google.gwt.user.client.History;

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
	public void bindBootstrapPopover(String id) {
		_bindBootstrapPopover(id);
	}

	private static native void _bindBootstrapPopover(String id) /*-{
		$wnd.jQuery('#'+id).popover();
	}-*/;

}
