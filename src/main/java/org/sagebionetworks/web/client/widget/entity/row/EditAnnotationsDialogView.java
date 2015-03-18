package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EditAnnotationsDialogView extends IsWidget {
	
	public interface Presenter {
		
		/**
		 * Configure the dialog
		 */
		void configure(EntityBundle bundle, EntityUpdatedHandler updateHandler);
		
		/**
		 * Called when the save button is clicked in the dialog
		 */
		void onSave();
		
		/**
		 * Called when the cancel button is clicked in the dialog
		 */
		void onCancel();
		
		/**
		 * Called when the add button is clicked in the dialog
		 */
		void onAddNewAnnotation();
		/**
		 * From the annotation editor
		 */
		void onAnnotationTypeChange(ANNOTATION_TYPE newType, AnnotationEditor editor);
		
		/**
		 * From the annotation editor
		 */
		void onAnnotationDeleted(AnnotationEditor editor);
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
	
	void addAnnotationEditor(Widget editor);
	void replaceAnnotationEditor(Widget oldEditor, Widget newEditor);
	void removeAnnotationEditor(Widget editor);
	void clearAnnotationEditors();
	
	void showInfo(String title, String message);

}
