package org.sagebionetworks.web.unitclient.widget.table.v2;

import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowEditor;

/**
 * Stub for ColumnModelTableRowEditor
 * @author jmhill
 *
 */
public class ColumnModelTableRowEditorStub extends ColumnModelTableRowStub implements ColumnModelTableRowEditor {

	Presenter presenter;
	boolean sizeFieldVisible;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSizeFieldVisible(boolean visible) {
		this.sizeFieldVisible = visible;
	}

}
