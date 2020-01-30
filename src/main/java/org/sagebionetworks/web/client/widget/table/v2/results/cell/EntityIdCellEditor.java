package org.sagebionetworks.web.client.widget.table.v2.results.cell;

// import java.util.regex.Pattern;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;

/**
 * Entity cell editor only allows values of the form "syn132.45"
 * 
 * @author jhill
 *
 */
public class EntityIdCellEditor extends AbstractCellEditor implements CellEditor {

	public static final String MUST_BE_OF_THE_FORM_SYN123 = "Must be in the form of a Synapse ID. For example: 'syn123'";
	public static final String SYN_PREFIX = "syn";
	public static final String PLACE_HOLDER = "Example: 'syn123'";
	public static final RegExp SYN_PATTERN = RegExp.compile("^\\s*(syn)?\\d+(\\.(\\d)+)?\\s*$", "i");


	@Inject
	public EntityIdCellEditor(CellEditorView view) {
		super(view);
		view.setPlaceholder(PLACE_HOLDER);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(this.getValue());
		if (value != null) {
			boolean isValid = SYN_PATTERN.test(value);
			if (!isValid) {
				this.view.setValidationState(ValidationState.ERROR);
				this.view.setHelpText(MUST_BE_OF_THE_FORM_SYN123);
			} else {
				this.view.setValidationState(ValidationState.NONE);
				this.view.setHelpText("");
			}
			return isValid;
		}
		return true;
	}


}
