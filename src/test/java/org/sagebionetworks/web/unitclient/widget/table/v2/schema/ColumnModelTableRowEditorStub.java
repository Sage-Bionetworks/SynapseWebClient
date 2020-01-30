package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Stub for ColumnModelTableRowEditor
 * 
 * @author jmhill
 *
 */
public class ColumnModelTableRowEditorStub extends ColumnModelTableRowStub implements ColumnModelTableRowEditorWidget {

	private boolean isValid = true;
	private boolean isSetToBeDefaultFileViewColumn = false;
	private boolean canHaveDefault = true;

	@Override
	public IsWidget getWidget(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWidgetCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void configure(ColumnModel model, SelectionPresenter selectionPresenter) {
		ColumnModelUtils.applyColumnModelToRow(model, this);
		setSelectionPresenter(selectionPresenter);
	}

	@Override
	public boolean validate() {
		return isValid;
	}

	/**
	 * Override the is valid
	 * 
	 * @param isValid
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public void setToBeDefaultFileViewColumn() {
		isSetToBeDefaultFileViewColumn = true;
	}

	public boolean isSetToBeDefaultFileViewColumn() {
		return isSetToBeDefaultFileViewColumn;
	}

	@Override
	public void setCanHaveDefault(boolean canHaveDefault) {
		this.canHaveDefault = canHaveDefault;
	}

	public boolean canHaveDefault() {
		return canHaveDefault;
	}

}
