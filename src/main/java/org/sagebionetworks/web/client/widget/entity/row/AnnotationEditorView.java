package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationEditorView extends IsWidget {
	
	public interface Presenter{
		/**
		 * Called when delete is clicked
		 */
		void onDelete();
		/**
		 * Called when type is changed
		 */
		void onTypeChange(ANNOTATION_TYPE newType);
		/**
		 * Can ask if the edited Annotation values are valid (updates UI)
		 * @return
		 */
		boolean isValid();
		/**
		 * Configure this Annotation editor with an annotation and callback handlers
		 * @param annotation
		 * @param typeChangeCallback
		 * @param deletedCallback
		 */
		void configure(Annotation annotation, 
				CallbackP<ANNOTATION_TYPE> typeChangeCallback, 
				Callback deletedCallback);
		/**
		 * Ask the editor for the updated values (based on values entered in the view)
		 * @return
		 */
		List<String> getUpdatedValues();
		
		/**
		 * Called when a particular value is deleted from the view
		 * @param editor
		 */
		void onValueDeleted(CellEditor editor);
		
		void onEnterClicked();
	}
	/**
	 * Configure the view with an initial key, type, and editor (based on type)
	 * @param key
	 * @param type
	 * @param editor
	 */
	void configure(String key, ANNOTATION_TYPE type);
	/**
	 * Adds a new cell editor
	 * @param editor
	 */
	void addNewEditor(CellEditor editor);
}
