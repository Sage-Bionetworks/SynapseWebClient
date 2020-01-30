package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;
import com.google.inject.Inject;

/**
 * Table editor for columns of type DOUBLE.
 * 
 * @author jhill
 *
 */
public class DoubleCellEditor extends AbstractCellEditor implements CellEditor {

	public static final String VALUE_MUST_BE_A_DOUBLE = "Value must be a double (i.e.,'-1.234e-3').";

	@Inject
	public DoubleCellEditor(NumberCellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(this.getValue());
		if (value != null) {
			try {
				// if it parses it is valid.
				Double.parseDouble(value);
			} catch (NumberFormatException e) {
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(VALUE_MUST_BE_A_DOUBLE);
				return false;
			}
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

}
