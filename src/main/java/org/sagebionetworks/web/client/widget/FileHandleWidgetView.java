package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleWidgetView extends IsWidget {

	/**
	 * Show/hide the loading spinner.
	 * 
	 * @param b
	 */
	void setLoadingVisible(boolean b);

	/**
	 * Set the File name
	 * 
	 * @param fileName
	 */
	void setErrorText(String fileName);

	/**
	 * Is this view attached?
	 * 
	 * @return
	 */
	boolean isAttached();

	/**
	 * Set the anchor value.
	 * 
	 * @param fileName
	 * @param createAnchorHref
	 */
	void setAnchor(String fileName, String createAnchorHref);

	void setVisible(boolean visible);
}
