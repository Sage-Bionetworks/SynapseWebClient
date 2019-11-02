package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.Query;

public interface QueryResultsListener extends QueryExecutionListener {

	/**
	 * Called before a new query is run.
	 * 
	 * @param newQuery
	 */
	public void onStartingNewQuery(Query newQuery);
}
