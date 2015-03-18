package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

import com.google.gwt.user.client.ui.IsWidget;

public interface EditAnnotationsDialog extends IsWidget {
	
	public interface Presenter {
		
		/**
		 * Configure the dialog
		 */
		void configure(EntityBundle bundle, EntityUpdatedHandler updateHandler);
		
		/**
		 * Called when the save button is clicked
		 */
		void onSave();
		
		/**
		 * Called when the cancel button is clicked
		 */
		void onCancel();
		
		/**
		 * Called when the add button is clicked
		 */
		void onAddNewAnnotation();
		/**
		 * Called by the annotation editor
		 */
		void onAnnotationTypeChange();
	}

	void setPresenter(Presenter presenter);

	/**
	 * Show the editor.
	 */
	void showEditor();

	/**
	 * Hide the editor
	 */
	void hideEditor();
	
	/**
	 * Called before any service call.
	 */
	void setLoading();

	/**
	 * Show error message.
	 * @param message
	 */
	void showError(String message);

	/**
	 * Hide the alert.
	 */
	void hideErrors();
	
	void addAnnotationEditor(AnnotationEditorView editor);
	void removeAnnotationEditor(AnnotationEditorView editor);
}
