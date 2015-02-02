package org.sagebionetworks.web.client.widget.table.v2.results.cell;

/**
 * Abstraction for StringEditorCell.
 * @author John
 *
 */
public interface StringEditorCell extends CellEditor {

	/**
	 * The max size of a string.
	 * @param maximumSize
	 */
	void setMaxSize(Long maximumSize);

}
