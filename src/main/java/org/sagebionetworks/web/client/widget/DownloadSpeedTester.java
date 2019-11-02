package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DownloadSpeedTester {

	/**
	 * Run a download test, and report back bytes per second
	 * 
	 * @param callback
	 */
	void testDownloadSpeed(AsyncCallback<Double> callback);
}
