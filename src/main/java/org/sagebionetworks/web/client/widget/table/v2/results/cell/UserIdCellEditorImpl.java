package org.sagebionetworks.web.client.widget.table.v2.results.cell;

//import java.util.regex.Pattern;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;

/**
 * UserId cell editor
 * @author Jay
 *
 */
public class UserIdCellEditorImpl extends AbstractCellEditor implements EntityIdCellEditor{

	@Inject
	public UserIdCellEditorImpl(CellEditorView view) {
		super(view);
//		view.setPlaceholder(PLACE_HOLDER);
	}

	@Override
	public boolean isValid() {
//		String value = StringUtils.trimWithEmptyAsNull(this.getValue());
//		if(value != null){
//			boolean isValid = SYN_PATTERN.test(value);
//			if(!isValid){
//				this.view.setValidationState(ValidationState.ERROR);
//				this.view.setHelpText(MUST_BE_OF_THE_FORM_SYN123);
//			}else{
//				this.view.setValidationState(ValidationState.NONE);
//				this.view.setHelpText("");
//			}
//			return isValid;
//		}
		return true;
	}

	
}
