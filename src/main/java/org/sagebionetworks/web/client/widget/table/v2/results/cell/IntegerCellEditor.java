package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;
import com.google.inject.Inject;

/**
 * Integer (Java Long) cell editor.
 * 
 * @author jhill
 *
 */
public class IntegerCellEditor extends AbstractCellEditor implements CellEditor {

	public static final String VALUE_MUST_BE_AN_INTEGER = "Value must be an integer.";

	@Inject
	public IntegerCellEditor(NumberCellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(this.getValue());
		if (value != null) {
			try {
				// if it parses it is valid.
				Long.parseLong(value);
			} catch (NumberFormatException e) {
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(VALUE_MUST_BE_AN_INTEGER);
				return false;
			}
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

}
