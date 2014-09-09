package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

public interface QueryChangeHandler extends EntityUpdatedHandler{
	
	/**
	 * Call to notify of a query change.
	 * @param newQuery
	 */
	void onQueryChange(Query newQuery);
	
	/**
	 * Get the current query string.
	 * @return
	 */
	public Query getQueryString();
}
