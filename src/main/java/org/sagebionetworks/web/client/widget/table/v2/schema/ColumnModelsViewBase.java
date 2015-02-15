package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;

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
		public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler);
		
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

	/**
	 * Hide the alert.
	 */
	public void hideErrors();
	
}
