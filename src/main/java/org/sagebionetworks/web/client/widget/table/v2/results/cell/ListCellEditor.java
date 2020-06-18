package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.StringUtils;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

/**
 * An editor for list type columns renders raw json array!).
 */
public class ListCellEditor extends AbstractCellEditor implements CellEditor {

	public static final String MUST_BE = "Must be ";
	public static final String CHARACTERS_OR_LESS = " characters or less";
	public static final String ITEMS_OR_LESS = " items or less";
	public static final String VALID_JSON_ARRAY = " a valid json array";
	JSONArrayAdapter jsonArrayAdapter;
	GWTWrapper gwt;
	Long maxListLength;
	Long maximumSize;

	@Inject
	public ListCellEditor(CellEditorView view, JSONArrayAdapter jsonArrayAdapter, GWTWrapper gwt) {
		super(view);
		this.jsonArrayAdapter = jsonArrayAdapter;
		this.gwt = gwt;
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(this.getValue());
		if (value != null) {
			// parse value
			if (!gwt.isValidJSONArray(value)) {
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(MUST_BE + VALID_JSON_ARRAY);
				return false;
			}
			try {
				JSONArrayAdapter adapter = jsonArrayAdapter.createNewArray(value);
				if (maxListLength != null) {
					boolean isListLengthValid = adapter.length() <= maxListLength;
					if (!isListLengthValid) {
						view.setValidationState(ValidationState.ERROR);
						view.setHelpText(MUST_BE + maxListLength + ITEMS_OR_LESS);
						return false;
					}	
				}
				
				if (maximumSize != null) {
					// check that each value is under the max string length
					for (int i = 0; i < adapter.length(); i++) {
						if (adapter.getString(i).length() > maximumSize) {
							view.setValidationState(ValidationState.ERROR);
							view.setHelpText(MUST_BE + maximumSize + CHARACTERS_OR_LESS);
							return false;
						}					
					}
				}
			} catch (JSONObjectAdapterException e) {
				// gwt.isValidJSONArray() will catch json parsing errors.
				// Is this now unreachable?
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(MUST_BE + VALID_JSON_ARRAY);
				return false;
			}
			
			
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

	public void setMaxSize(Long maximumSize) {
		this.maximumSize = maximumSize;
	}
	public void setMaxListLength(Long maximumListLength) {
		this.maxListLength = maximumListLength;
	}

}
