package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.Widget;

public interface JSONListCellEditorView extends CellEditorView {
	public interface Presenter{
		void onEditButtonClick();
	}


	void setEditor(JSONListCellEditor editor);

	void addEditorToPage(Widget editJSONModalWidget);
}
