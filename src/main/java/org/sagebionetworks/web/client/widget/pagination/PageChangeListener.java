package org.sagebionetworks.web.client.widget.pagination;

/**
 * Abstraction for a page change lister.
 * 
 * @author John
 *
 */
public interface PageChangeListener {

	/**
	 * A page change event includes a new offset. The limit does not change.
	 * 
	 * @param newOffset The new offset. Zero is the first element and n-1 is the last.
	 */
	public void onPageChange(Long newOffset);
}
