package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.repo.model.EntityBundle;

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
		public void configure(EntityBundle bundle, boolean isEditable);
		
		/**
		 * Called when the save button is pressed
		 */
		public void onSave();
	}

	public void setPresenter(Presenter presenter);

	public void setViewer(ColumnModelsView viewer);

	public void setEditor(IsWidget editor);
	public void setJobTrackingWidget(IsWidget jobTrackingWidget);
	public void setJobTrackingWidgetVisible(boolean visible);
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
