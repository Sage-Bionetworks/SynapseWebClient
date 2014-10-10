package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstraction between the UploadCSVPreviewWidget and its view.
 * 
 * @author John
 *
 */
public interface UploadCSVConfigurationView extends IsWidget {
	
	/**
	 * Business logic goes here.
	 *
	 */
	interface Presenter{
		
	}

	/**
	 * Bind the view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Set the table name.
	 * @param fileName
	 */
	public void setTableName(String fileName);

	/**
	 * Add the tracking widget to the view.
	 * @param tracker
	 */
	public void setTrackingWidget(Widget tracker);

	/**
	 * Show or hide the tracker widget
	 * @param visible
	 */
	public void setTrackerVisible(boolean visible);

	/**
	 * Show or hide the preview table.
	 * @param b
	 */
	public void setPreviewVisible(boolean visible);

	/**
	 * Add the preview widget.s
	 * @param uploadPreviewWidget
	 */
	public void setPreviewWidget(Widget uploadPreviewWidget);
	
	/**
	 * Show the spinner with the following text
	 * @param text
	 */
	public void showSpinner(String text);
	
	/**
	 * Hide the spinner.
	 */
	public void hideSpinner();

	/**
	 * Get the name of the table from the box.
	 * @return
	 */
	public String getTableName();
}
