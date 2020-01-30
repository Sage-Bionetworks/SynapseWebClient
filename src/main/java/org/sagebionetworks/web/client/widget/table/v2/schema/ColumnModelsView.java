package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * An editable widget of a list of ColumnModels
 * 
 * @author jmhill
 * 
 */
public interface ColumnModelsView extends IsWidget {

	public interface EditHandler {
		/**
		 * Called when the edit button is pressed
		 */
		public void onEditColumns();
	}

	/**
	 * All business logic for this view belongs in the presenter.
	 * 
	 */
	public interface Presenter {

		/**
		 * Get the current list of ColumnModels.
		 * 
		 * @return
		 */
		public List<ColumnModel> getEditedColumnModels();

		/**
		 * Validate the column models for this view. If there are any errors they will be reflected in the
		 * UI.
		 * 
		 * @return return True if the current ColumnModels are valid. False, if there are any errors that
		 *         would prevent the column models from being saved.
		 */
		public boolean validateModel();

		/**
		 * Add a new column to the table.
		 * 
		 * @return
		 */
		public ColumnModelTableRowEditorWidget addNewColumn();

		/**
		 * Toggle the selection.
		 */
		public void toggleSelect();

		/**
		 * On select all.
		 */
		public void selectAll();

		/**
		 * Select none
		 */
		public void selectNone();

		/**
		 * Move the selected item up.
		 */
		public void onMoveUp();

		/**
		 * Move the selected item down.
		 */
		public void onMoveDown();

		/**
		 * Delete the selected columns.
		 */
		public void deleteSelected();

		/**
		 * Add column models based on the default entity view fields
		 */
		void onAddDefaultViewColumns();

		/**
		 * Add column models based on the annotations found in the view scope
		 */
		void onAddAnnotationColumns();

	}


	/**
	 * Connect the view to the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEditHandler(EditHandler handler);

	/**
	 * Add a row to the table.
	 * 
	 * @param row
	 */
	void addColumn(ColumnModelTableRow row);

	/**
	 * 
	 * @param row
	 * @param index
	 */
	void moveColumn(ColumnModelTableRow row, int index);

	/**
	 * Set the view editable
	 * 
	 * @param isEditable
	 */
	void configure(ViewType type, boolean isEditable);


	/**
	 * Determines the state of the delete button
	 * 
	 * @param b
	 */
	public void setCanDelete(boolean canDelete);

	/**
	 * Is the delete button enabled?
	 * 
	 * @return
	 */
	public boolean isDeleteEnabled();

	/**
	 * Determines the state of the move up button.
	 * 
	 * @param b
	 */
	public void setCanMoveUp(boolean canMoveUp);

	/**
	 * Is the move down button enabled.
	 * 
	 * @return
	 */
	public boolean isMoveUpEnabled();

	/**
	 * Determines the state of the move down button.
	 * 
	 * @param canMoveDown
	 */
	public void setCanMoveDown(boolean canMoveDown);

	/**
	 * Is the move down button enabled?
	 * 
	 * @return
	 */
	public boolean isMoveDownEnabled();

	/**
	 * Is the Add Default View Columns Button visible?
	 * 
	 * @param visible
	 */
	void setAddDefaultViewColumnsButtonVisible(boolean visible);

	/**
	 * Is the Add Annotation Columns Button visible?
	 * 
	 * @param visible
	 */
	void setAddAnnotationColumnsButtonVisible(boolean visible);

	/**
	 * The view can be used as a column viewer or as a column editor.
	 *
	 */
	public enum ViewType {
		VIEWER, EDITOR
	}

	void showErrorMessage(String message);

	void addButton(IsWidget widget);
}
