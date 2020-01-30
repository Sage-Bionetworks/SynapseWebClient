package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This view shows the options that for a table query download.
 * 
 * @author John
 *
 */
public interface CreateDownloadPageView extends IsWidget {

	/**
	 * The type of file to create.
	 * 
	 * @param csv
	 */
	void setFileType(FileType csv);

	/**
	 * The type of file to create.
	 * 
	 * @return
	 */
	FileType getFileType();

	/**
	 * Should the first row be a header row?
	 * 
	 * @param include
	 */
	void setIncludeHeaders(boolean include);

	/**
	 * Should the first row be a header row?
	 * 
	 * @return
	 */
	boolean getIncludeHeaders();

	/**
	 * Should the row metadata be included in the file?
	 * 
	 * @param include
	 */
	void setIncludeRowMetadata(boolean include);

	/**
	 * Should the row metadata be included in the file?
	 * 
	 * @return
	 */
	boolean getIncludeRowMetadata();

	/**
	 * Add the tracker widget to the view.
	 * 
	 * @param trackerWidget
	 */
	public void addTrackerWidget(IsWidget trackerWidget);

	/**
	 * Show/hide the tracker panel.
	 * 
	 * @param visible
	 */
	public void setTrackerVisible(boolean visible);

}
