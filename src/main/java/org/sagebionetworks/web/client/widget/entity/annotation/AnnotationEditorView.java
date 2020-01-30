package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationEditorView extends IsWidget {

	public interface Presenter {
		/**
		 * Called when delete is clicked
		 */
		void onDelete();

		/**
		 * Called when type is changed
		 */
		void onTypeChange(int typeIndex);

		/**
		 * Can ask if the edited Annotation values are valid (updates UI)
		 * 
		 * @return
		 */
		boolean isValid();

		/**
		 * Called when a particular value is deleted from the view
		 * 
		 * @param editor
		 */
		void onValueDeleted(CellEditor editor);

		void onAddNewValue();
	}

	/**
	 * Configure the view with an initial key, type
	 * 
	 * @param key
	 */
	void configure(String key, int typeIndex);

	/**
	 * Adds a new cell editor
	 * 
	 * @param editor
	 */
	void addNewEditor(CellEditor editor);

	String getKey();

	/**
	 * Set the validation state of the key.
	 * 
	 * @param state
	 */
	public void setKeyValidationState(ValidationState state);

	/**
	 * Set the help text of the key.
	 * 
	 * @param help
	 */
	public void setKeyHelpText(String help);

	void setPresenter(Presenter presenter);

	void clearValueEditors();

	void setTypeOptions(List<String> types);
}
