package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;

/**
 * A cell that can be toggled between a renderer and editor.
 * 
 * @author John
 *
 */
public interface ToggleCell extends Cell {
	
	/**
	 * Toggle the editing of this cell.
	 * 
	 * @param isEditing
	 */
	public void toggleEdit(boolean isEditing);
}
