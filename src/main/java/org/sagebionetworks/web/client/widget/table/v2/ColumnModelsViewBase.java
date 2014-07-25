package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;

public interface ColumnModelsViewBase extends IsWidget {
	
	public interface Presenter {
		
	}

	public void setPresenter(Presenter presenter);

	public void showError(String string);

	public void setViewer(ColumnModelsView viewer);

	public void setEditor(ColumnModelsView editor);

	public void setEditable(boolean isEditable);

	/**
	 * Show the editor.
	 */
	public void showEditor();
}
