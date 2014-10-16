package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.Callback;

public interface UploadView {
	void showUploadDialog(String entityId);
	void showQuizInfoDialog(boolean isCertificationRequired, Callback remindMeLaterCallback);
	void showErrorMessage(String message);	
}
