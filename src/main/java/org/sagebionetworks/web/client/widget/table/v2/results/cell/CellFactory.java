package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.table.ColumnModel;

/**
 * Factory for generating cell renderer and editors for 
 * @author jmhill
 *
 */
public interface CellFactory {
	
	/**
	 * Create a read-only renderer for a cell.
	 * @param type
	 * @param value
	 * @return
	 */
	public Cell createRenderer(ColumnModel model);


	/**
	 * Crate an editor for a cell.
	 * @param type
	 * @param value
	 * @return
	 */
	public CellEditor createEditor(ColumnModel model);
	
	/**
	 * Create a form based editor for a cell.
	 * @param type
	 * @param value
	 * @return
	 */
	public CellEditor createFormEditor(ColumnModel model);
}
