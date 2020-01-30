package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Abstraction for a row selection listener.
 * 
 * @author jmhill
 *
 */
public interface RowSelectionListener {

	/**
	 * Called when a row changes its selection.
	 */
	public void onSelectionChanged();

}
