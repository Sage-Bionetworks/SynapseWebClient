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
public class IntegerCellEditorImpl extends AbstractCellEditor implements IntegerCellEditor {

	public static final String VALUE_MUST_BE_AN_INTEGER = "Value must be an integer.";

	@Inject
	public IntegerCellEditorImpl(CellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.trimWithEmptyAsNull(this.getValue());
		if(value != null){
			try{
				// if it parses it is valid.
				Long.parseLong(value);
			}catch(NumberFormatException e){
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
