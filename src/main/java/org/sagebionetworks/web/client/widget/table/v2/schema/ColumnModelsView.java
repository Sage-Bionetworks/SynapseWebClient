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
		 * Validate the column models for this view. If there are any errors they
		 * will be reflected in the UI.
		 * 
		 * @return return True if the current ColumnModels are valid. False, if
		 *         there are any errors that would prevent the column models from
		 *         being saved.
		 */
		public boolean validateModel();
		
		/**
		 * Add a new column to the table.
		 * @return 
		 */
		public ColumnModelTableRowEditorWidget addNewColumn();
		
		/**
		 * Called when the edit button is pressed
		 */
		public void onEditColumns();


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
		 * Add column models based on the default file entity fields
		 */
		void onAddDefaultFileColumns();
		
		/**
		 * Add column models based on annotations (automatically discovered based on the view scope)
		 */
		void onAddAllAnnotations();
	}

	/**
	 * Connect the view to the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Add a row to the table.
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
	 * @param isEditable
	 */
	void configure(ViewType type, boolean isEditable);
	

	/**
	 * Determines the state of the delete button
	 * @param b
	 */
	public void setCanDelete(boolean canDelete);
	
	/**
	 * Is the delete button enabled?
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
	 * Is the Add Default File Columns Button visible?
	 * @param visible
	 */
	void setAddDefaultFileColumnsButtonVisible(boolean visible);
	
	/**
	 * Is the Add All Annotations Button visible?
	 * @param visible
	 */
	void setAddAllAnnotationsButtonVisible(boolean visible);
	
	/**
	 * The view can be used as a column viewer or as a column editor.
	 *
	 */
	public enum ViewType {
		VIEWER,
		EDITOR
	}

}
