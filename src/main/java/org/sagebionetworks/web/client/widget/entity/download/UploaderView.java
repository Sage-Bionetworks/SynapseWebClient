package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface UploaderView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean isEntity, String parentEntityId, boolean isDirectUploadSupported);
	
	public int getDisplayHeight();

	public int getDisplayWidth();
	public void submitForm();
	public void hideLoading();
	public void updateProgress(double value, String text);
	public void showProgressBar();
	public void showConfirmDialog(String title, String message, Callback yesCallback, Callback noCallback);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		String getDefaultUploadActionUrl();

		void setExternalFilePath(String path, String name);
		
		void handleUpload(String fileName);
		
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
	}

	public void setShowCancelButton(boolean showCancel);
	public void showUploaderUI();
}
