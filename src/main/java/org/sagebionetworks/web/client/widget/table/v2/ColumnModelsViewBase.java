package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;

import com.google.gwt.user.client.ui.IsWidget;

public interface ColumnModelsViewBase extends IsWidget {
	
	public interface Presenter {
		
		/**
		 * Configure a newly created view.
		 * 
		 * @param headerText
		 * @param models
		 * @param isEditabl
		 */
		public void configure(String tableId, List<ColumnModel> models, boolean isEditable);
		
		/**
		 * Called when the save button is pressed
		 */
		public void onSave();
	}

	public void setPresenter(Presenter presenter);

	public void setViewer(ColumnModelsView viewer);

	public void setEditor(ColumnModelsView editor);

	/**
	 * Show the editor.
	 */
	public void showEditor();

	/**
	 * Hide the editor
	 */
	public void hideEditor();
	
	/**
	 * Called before any service call.
	 */
	public void setLoading();

	/**
	 * Show error message.
	 * @param message
	 */
	void showError(String message);
}
