package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;

import com.google.inject.Inject;

/**
 * An editor for a string.
 * 
 * @author John
 *
 */
public class StringEditorCellImpl extends AbstractCellEditor implements StringEditorCell {
	
	public static final String MUST_BE = "Must be ";
	public static final String CHARACTERS_OR_LESS = " characters or less";

	Long maximumSize;
	
	@Inject
	public StringEditorCellImpl(CellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.trimWithEmptyAsNull(this.getValue());
		if(value != null && maximumSize != null){
			boolean valid = value.length() <= maximumSize;
			if(!valid){
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(MUST_BE+maximumSize+CHARACTERS_OR_LESS);
				return false;
			}
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

	@Override
	public void setMaxSize(Long maximumSize) {
		this.maximumSize = maximumSize;
	}
	
}
