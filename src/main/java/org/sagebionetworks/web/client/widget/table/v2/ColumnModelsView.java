package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;

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
		 */
		public void addNewColumn();
		
		/**
		 * Called when the edit button is pressed
		 */
		public void onEditColumns();

		/**
		 * Called when there is a selection change.
		 * @param columnId
		 * @param isSelected
		 */
		public void columnSelectionChanged(String columnId, Boolean isSelected);
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
	 * Set the view editable
	 * @param isEditable
	 */
	void configure(ViewType type, boolean isEditable);

	/**
	 * The view can be used as a column viewer or as a column editor.
	 *
	 */
	public enum ViewType {
		VIEWER,
		EDITOR
	}

}
