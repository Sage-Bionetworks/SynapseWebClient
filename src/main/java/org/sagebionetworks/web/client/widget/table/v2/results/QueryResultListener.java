package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Abstraction for listening to query results.
 * 
 * @author John
 *
 */
public interface QueryResultListener {
	
	/**
	 * Called when query execution starts.
	 */
	public void queryExecutionStarted();
	
	/**
	 * Called when query execution finishes.
	 */
	public void queryExecutionFinished();
}
