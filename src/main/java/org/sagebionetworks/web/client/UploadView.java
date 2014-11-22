package org.sagebionetworks.web.client;


public interface UploadView {
	void showUploadDialog(String entityId);
	void showQuizInfoDialog();
	void showErrorMessage(String message);	
}
