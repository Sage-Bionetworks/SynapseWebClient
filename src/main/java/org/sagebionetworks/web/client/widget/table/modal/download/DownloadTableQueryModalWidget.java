package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This widget is a modal dialog used to download the results of a table query.
 * Thsi widget should be added to the page to work correctly.
 * 
 * @author jhill
 *
 */
public interface DownloadTableQueryModalWidget extends IsWidget {
	
	/**
	 * Configure this widget before using it.
	 * @param sql
	 */
	public void configure(String sql);
	
	/**
	 * Show the dialog
	 */
	public void showDialog();

}
