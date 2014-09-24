package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.table.TableEntity;
/**
 * Abstraction for a table creation handler.
 * @author John
 *
 */
public interface TableCreatedHandler {

	/**
	 * Called when a new table entity is created.
	 * @param newEntity The newly created entity.
	 */
	public void tableCreated(TableEntity table);
}
