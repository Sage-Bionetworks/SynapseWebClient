package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileCellRendererView extends IsWidget {

	/**
	 * Show/hide the loading spinner.
	 * @param b
	 */
	void setLoadingVisible(boolean b);

	/**
	 * Set the File name
	 * @param fileName
	 */
	void setFileName(String fileName);

	/**
	 * Is this view attached?
	 * @return
	 */
	boolean isAttached();

}
