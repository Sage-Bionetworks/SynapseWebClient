package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleUploadView extends IsWidget {
	
	public interface Presenter{
		/**
		 * Called when a file is selected.
		 */
		public void onFileSelected();
	}
	
	/**
	 * Bind the presenter to the view.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void showProgress(boolean b);

	public void setInputEnabled(boolean b);

	public String getInputId();

	public void updateProgress(double d, String progressText);

	public void showError(String error);

	void resetForm();

	/**
	 * Hide the alert.
	 */
	public void hideError();

	/**
	 * Set the text for the upload button.
	 * @param buttonText
	 */
	public void setButtonText(String buttonText);

}
