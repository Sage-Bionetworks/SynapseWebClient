package org.sagebionetworks.web.client.widget.table;

public interface QueryChangeHandler {
	
	/**
	 * Call to notify of a query change.
	 * @param newQuery
	 */
	void onQueryChange(String newQuery);
	
	/**
	 * Get the current query string.
	 * @return
	 */
	public String getQueryString();
}
