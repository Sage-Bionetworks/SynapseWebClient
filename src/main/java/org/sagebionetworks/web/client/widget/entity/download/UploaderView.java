package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface UploaderView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean isEntity, String parentEntityId);
	
	public void submitForm(String actionUrl);
	public void hideLoading();
	public void updateProgress(double value, String text);
	public void showProgressBar();
	public void showConfirmDialog(String message, Callback yesCallback, Callback noCallback);
	void resetToInitialState();
	void showNoFilesSelectedForUpload();
	void enableMultipleFileUploads(boolean isEnabled);
	void setShowCancelButton(boolean showCancel);
	void showUploaderUI();
	void triggerUpload();
	
	void showUploadingToSynapseStorage(String banner);
	void showUploadingToExternalStorage(String url, String banner);
	
	/**
	 * SFTP requires username and password, so prompt for it in the upload form.
	 * @return
	 */
	String getExternalUsername();
	/**
	 * SFTP requires username and password, so prompt for it in the upload form.
	 * @return
	 */
	String getExternalPassword();
	
	void showExternalCredentialsRequiredMessage();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void setExternalFilePath(String path, String name);
		
		void handleUploads();
		
		/**
		 * 
		 * @param resultHtml
		 */
		void handleSubmitResult(String resultHtml);
		
		void clearHandlers();
		
		/**
		 * Called when cancel is clicked in the view
		 */
		void cancelClicked();
		
		void disableMultipleFileUploads();
	}
}
