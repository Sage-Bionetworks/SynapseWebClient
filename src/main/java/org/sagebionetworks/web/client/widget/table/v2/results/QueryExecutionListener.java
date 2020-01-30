package org.sagebionetworks.web.client.widget.table.v2.results;


/**
 * Abstraction for a listener that needs to know when a query is executing.
 * 
 * @author John
 *
 */
public interface QueryExecutionListener {

	/**
	 * Called when query execution starts.
	 */
	public void queryExecutionStarted();

	/**
	 * Called when query execution finishes.
	 * 
	 * @param wasSuccessful True if the query was successful, else false.
	 * @param resultsEditable True the the query results are editable.
	 */
	public void queryExecutionFinished(boolean wasSuccessful, boolean resultsEditable);
}
