package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.ui.Focusable;

public interface CellEditor extends Cell, HasKeyDownHandlers, Focusable {

	/**
	 * Validate the contents of the editor.
	 */
	public boolean isValid();
}
