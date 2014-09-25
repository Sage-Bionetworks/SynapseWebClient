package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;

/**
 * Simple abstraction for a row in table.
 * 
 * @author jhill
 *
 */
public interface TableRow {

	/**
	 * Iterate over the cells of this row.
	 * 
	 * @return
	 */
	Iterable<Cell> getCells();
	/**
	 * Get the cell at the given index.
	 * @param index
	 * @return
	 */
	public Cell getCell(int index);
	/**
	 * The number of cells in this row.
	 * @return
	 */
	public int getCellCount();
}
