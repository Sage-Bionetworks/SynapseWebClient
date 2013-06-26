package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface UploaderView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean isExternalSupported);
	
	public int getDisplayHeight();

	public int getDisplayWidth();
	public void submitForm();
	public void hideLoading();
	public boolean isNewlyRestricted();
	public void updateProgress(double value, String text);
	public void showProgressBar();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		String getDefaultUploadActionUrl(boolean isRestricted);

		void setExternalFilePath(String path, String name, boolean isNewlyRestricted);
		
		void handleUpload(String fileName);
		
		/**
		 * 
		 * @param resultHtml
		 */
		void handleSubmitResult(String resultHtml, boolean isNewlyRestricted);
		
		/**
		 * returns true iff the dataset is currently (initially) restricted
		 * @return
		 */
		boolean isRestricted();

		void clearHandlers();
	}

}
