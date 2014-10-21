package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view of the options for table CSV file download.
 * @author jhill
 *
 */
public interface DownloadTableQueryModalView  extends IsWidget{
	
	/**
	 * Methods the view can call on the widget.
	 *
	 */
	public interface Presenter{
		
		/**
		 * Called when the user selects the primary button.
		 */
		void onPrimary();
	}

	/**
	 * The type of file to create.
	 * @param csv
	 */
	void setFileType(FileType csv);
	/**
	 * The type of file to create.
	 * @return
	 */
	FileType getFileType();

	/**
	 * Should the first row be a header row?
	 * @param include
	 */
	void setIncludeHeaders(boolean include);
	
	/**
	 * Should the first row be a header row?
	 * @return
	 */
	boolean getIncludeHeaders();
	
	/**
	 * Should the row metadata be included in the file?
	 * @param include
	 */
	void setIncludeRowMetadata(boolean include);
	/**
	 * Should the row metadata be included in the file?
	 * @return
	 */
	boolean getIncludeRowMetadata();
	/**
	 * Add the tracker widget to the view.
	 * @param trackerWidget
	 */
	public void addTrackerWidget(IsWidget trackerWidget);
	
	/**
	 * Show/hide the tracker panel.
	 * @param visible
	 */
	public void setTrackerVisible(boolean visible);
	
	/**
	 * Set the error message
	 * @param message
	 */
	public void setErrorMessage(String message);
	
	/**
	 * Show/hide the error message;
	 * @param visibile
	 */
	public void setErrorMessageVisible(boolean visibile);
	
	/**
	 * Show the modal.
	 */
	void show();

	/**
	 * Called to enable or disable the primary button
	 * @param loading
	 */
	void setLoading(boolean loading);
	
	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	/**
	 * Hide the dialog.
	 */
	void hide();
}
