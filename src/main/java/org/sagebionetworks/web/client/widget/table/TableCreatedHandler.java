package org.sagebionetworks.web.client.widget.table;

/**
 * Abstraction for a table creation handler.
 * 
 * @author John
 *
 */
public interface TableCreatedHandler {

	/**
	 * Called when a new table entity is created.
	 * 
	 * @param newEntity The newly created entity.
	 */
	public void tableCreated();
}
