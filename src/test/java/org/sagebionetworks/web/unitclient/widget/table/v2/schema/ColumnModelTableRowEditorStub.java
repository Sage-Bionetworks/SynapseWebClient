package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditor;

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

}
