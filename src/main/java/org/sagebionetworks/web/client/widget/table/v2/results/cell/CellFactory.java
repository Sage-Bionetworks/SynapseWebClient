package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

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
	public Cell createRenderer(ColumnTypeViewEnum type);


	/**
	 * Crate an editor for a cell.
	 * @param type
	 * @param value
	 * @return
	 */
	public Cell createEditor(ColumnTypeViewEnum type);
}
