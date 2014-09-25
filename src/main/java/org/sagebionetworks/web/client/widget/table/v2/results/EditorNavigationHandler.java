package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Abstraction for handler that listens to navigation key events of table editors.
 * When a navigation event occurs, focus will be passed to the appropriate editor.
 * 
 * @author jhill
 *
 */
public interface EditorNavigationHandler {
	
	/**
	 * Bind a new row to this handler.
	 * Must call {@link #recalculateAddresses()} after all row changes have been made.
	 * 
	 * @param row
	 */
	public void bindRow(TableRow row);
	
	/**
	 * Remove a row from this handler.
	 * Must call {@link #recalculateAddresses()} after all row changes have been made.
	 * @param row
	 */
	public void removeRow(TableRow row);
	
	/**
	 * Recalculate the address of each editor.
	 * This method should be called once after all row changes have been made.
	 */
	public void recalculateAddresses();
}
