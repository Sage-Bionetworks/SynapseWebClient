package org.sagebionetworks.web.client.widget.pagination;

/**
 * Sorting and paging listener.
 *
 * @author jhill
 *
 */
public interface PagingAndSortingListener extends PageChangeListener {
	/**
	 * Toggle the sort on a header.
	 * 
	 * @param header
	 */
	public void onToggleSort(String header);
	
}