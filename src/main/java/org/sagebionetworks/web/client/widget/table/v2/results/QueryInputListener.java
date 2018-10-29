package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Abstraction for a listener to input queries.
 * 
 * @author John
 *
 */
public interface QueryInputListener {
	
	/**
	 * Called when the user explicitly execute a query.
	 * 
	 * @param sql
	 */
	public void onExecuteQuery(String sql);

	/**
	 * Called when the user selects the edit button.
	 */
	public void onEditResults();

	/**
	 * Called when the user clicks the download button
	 */
	public void onDownloadResults();
	
	/**
	 * Called when the user clicks the show query button
	 */
	public void onShowQuery();
	/**
	 * user clicked download files (from view) button.
	 */
	public void onShowDownloadFilesProgrammatically();
	void onAddToDownloadList();
}
