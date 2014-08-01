package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.CallbackP;

public interface UploadView {
	void showUploadDialog(String entityId);
	void showQuizInfoDialog(CallbackP<Boolean> callback);
	void showErrorMessage(String message);	
}
