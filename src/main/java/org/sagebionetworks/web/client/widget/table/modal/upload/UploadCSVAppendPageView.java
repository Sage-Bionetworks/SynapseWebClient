package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This view is shown when a CSV is to be appended to an existing table.
 * 
 * @author John
 *
 */
public interface UploadCSVAppendPageView extends IsWidget {

	/**
	 * Add the job tracking widget to this view.
	 * 
	 * @param jobTrackingWidget
	 */
	void addJobTrackingWidget(IsWidget jobTrackingWidget);

	/**
	 * Show/hide the tracking widget.
	 * 
	 * @param visible
	 */
	void setTrackingWidgetVisible(boolean visible);

	void showErrorDialog(String message);
}
