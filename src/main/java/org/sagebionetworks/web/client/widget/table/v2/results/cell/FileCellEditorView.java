package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

public interface FileCellEditorView extends IsWidget, TakesValue<String>,
		Focusable {

	/**
	 * Contract between the view and presenter business logic.
	 * 
	 */
	interface Presenter {

		/**
		 * Show/hide the collapse panel
		 */
		public void onToggleCollapse();

		/**
		 * Called when the user clicks the upload file button of the modal
		 * dialog.
		 */
		public void onUploadFile();

		/**
		 * Cancel the uplaod.
		 */
		public void onCancelUpload();
	}

	/**
	 * Bind the view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Show the upload modal dialog.
	 */
	public void showCollapse();

	/**
	 * Hide the upload modal dialog.
	 */
	public void hideCollapse();

	/**
	 * Add the file input widget
	 * 
	 * @param fileInputWidget
	 */
	public void addFileInputWidget(IsWidget fileInputWidget);

	/**
	 * Set an error message.
	 * @param string
	 */
	public void showErrorMessage(String message);

	/**
	 * Hide the error message.
	 */
	public void hideErrorMessage();

	/**
	 * Set the loading state of the upload button.
	 */
	public void setUploadButtonLoading();

	/**
	 * Reset the state of the upload button.
	 */
	public void resetUploadButton();

	/**
	 * Set an error message for a value.
	 * @param string
	 */
	public void setValueError(String string);

	/**
	 * Clear the help and state of the value.
	 */
	public void clearValueError();

	/**
	 * Toggle the collapse
	 */
	public void toggleCollapse();

}
