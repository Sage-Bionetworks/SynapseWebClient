package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditor;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Stub for ColumnModelTableRowEditor
 * @author jmhill
 *
 */
public class ColumnModelTableRowEditorStub extends ColumnModelTableRowStub implements ColumnModelTableRowEditor {

	TypePresenter presenter;
	boolean sizeFieldVisible;
	
	@Override
	public void setTypePresenter(TypePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSizeFieldVisible(boolean visible) {
		this.sizeFieldVisible = visible;
	}

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

}
