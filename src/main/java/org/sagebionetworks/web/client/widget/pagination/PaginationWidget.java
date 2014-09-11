package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a PaginationWidget.
 * 
 * @author jmhill
 *
 */
public interface PaginationWidget extends IsWidget {

	/**
	 * Configure this widget with a limit, offset and count.
	 * @param limit The limit sets the page size.
	 * @param offset The offset into the results. Zero is the first result, n-1 is the last.
	 * @param count The total number of objects across all pages.
	 * @param listener Listens to page change events from this widget.
	 */
	public void configure(Long limit, Long offset, Long count, PageChangeListener listener);
	
	
}
