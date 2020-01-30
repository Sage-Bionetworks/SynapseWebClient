package org.sagebionetworks.web.client.widget.entity.annotation;

import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

/**
 * Factory for generating cell renderer and editors for Annotations
 *
 */
public interface AnnotationCellFactory {

	/**
	 * Crate an editor for a cell.
	 * 
	 * @return
	 */
	public CellEditor createEditor(AnnotationsValue annotation);
}
