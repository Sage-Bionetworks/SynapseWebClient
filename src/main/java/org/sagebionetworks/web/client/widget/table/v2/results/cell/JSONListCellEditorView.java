package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;

public interface JSONListCellEditorView extends CellEditorView {
	public interface Presenter{
		void onEditButtonClick();
	}

	void setEditor(JSONListCellEditor editor);

	void addEditorToPage(Widget editJSONModalWidget);
}
