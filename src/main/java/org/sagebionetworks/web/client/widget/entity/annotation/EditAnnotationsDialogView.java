package org.sagebionetworks.web.client.widget.entity.annotation;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EditAnnotationsDialogView extends IsWidget {

	public interface Presenter {

		/**
		 * Configure the dialog
		 */
		void configure(EntityBundle bundle);

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
	 * 
	 * @param message
	 */
	void showError(String message);

	/**
	 * Hide the alert.
	 */
	void hideErrors();

	void addAnnotationEditor(Widget editor);

	void removeAnnotationEditor(Widget editor);

	void clearAnnotationEditors();

	void showInfo(String message);

}
