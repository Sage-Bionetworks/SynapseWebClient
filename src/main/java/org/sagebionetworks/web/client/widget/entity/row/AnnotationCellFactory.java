package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

/**
 * Factory for generating cell renderer and editors for Annotations 
 *
 */
public interface AnnotationCellFactory {
	
	/**
	 * Crate an editor for a cell.
	 * @return
	 */
	public CellEditor createEditor(ANNOTATION_TYPE type);
}
