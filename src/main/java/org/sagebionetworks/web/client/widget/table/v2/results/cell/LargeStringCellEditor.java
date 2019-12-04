package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import com.google.inject.Inject;

/**
 * An editor for a large string.
 *
 */
public class LargeStringCellEditor extends AbstractCellEditor implements CellEditor {
	@Inject
	public LargeStringCellEditor(LargeStringCellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}
}
