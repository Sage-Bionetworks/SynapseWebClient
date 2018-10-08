package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.ui.IsWidget;

public interface UploaderView extends IsWidget, SynapseView, HasAttachHandlers {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean isEntity, String parentEntityId);
	
	public void submitForm(String actionUrl);
	public void hideLoading();
	public void updateProgress(double value, String text, String uploadSpeed);
	public void showProgressBar();
	public void showConfirmDialog(String message, Callback yesCallback, Callback noCallback);
	void resetToInitialState();
	void enableMultipleFileUploads(boolean isEnabled);
	void setShowCancelButton(boolean showCancel);
	void showUploaderUI();
	void triggerUpload();
	void setExternalUrl(String url);
	void showErrorMessage(String title, String details);
	
	void showUploadingToSynapseStorage();
	void showUploadingBanner(String banner);
	void showUploadingToExternalStorage(String host, String banner);
	void showUploadingToS3DirectStorage(String endpoint, String banner);
	void setUploaderLinkNameVisible(boolean visible);
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
	
	String getS3DirectAccessKey();
	String getS3DirectSecretKey();
	
	void enableUpload();
	void setSelectedFilenames(String fileNames);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void setExternalFilePath(String path, String name, Long storageLocationId);
		
		void handleUploads();
		
		/**
		 * 
		 * @param resultHtml
		 */
		void handleSubmitResult(String resultHtml);
		
		/**
		 * Called when cancel is clicked in the view
		 */
		void cancelClicked();
		
		void disableMultipleFileUploads();
		Long getStorageLocationId();
	}
}
