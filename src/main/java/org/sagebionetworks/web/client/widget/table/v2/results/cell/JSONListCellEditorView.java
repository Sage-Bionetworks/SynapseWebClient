package org.sagebionetworks.web.client.widget.table.v2.results.cell;

public interface JSONListCellEditorView extends CellEditorView {
	public interface Presenter{
		void onEditButtonClick();
	}

	void setPresenter(Presenter editor);

}
