package org.sagebionetworks.web.client.widget.licenseddownloader;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LicenceServiceAsync {

	void logUserDownload(String username, String objectUri, String fileUri, AsyncCallback<Void> callback);
	
}
