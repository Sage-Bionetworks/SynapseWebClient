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
	
	
}
