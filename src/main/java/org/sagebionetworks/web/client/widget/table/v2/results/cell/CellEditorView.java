package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.base.HasPlaceholder;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a generic cell editor.
 * 
 * @author jhill
 *
 */
public interface CellEditorView extends IsWidget, TakesValue<String>, HasKeyDownHandlers, Focusable, HasPlaceholder {

	/**
	 * Set the validation state of the cell.
	 * 
	 * @param state
	 */
	public void setValidationState(ValidationState state);

	/**
	 * Set the help text of the cell.
	 * 
	 * @param help
	 */
	public void setHelpText(String help);
}
