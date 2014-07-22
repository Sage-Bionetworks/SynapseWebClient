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
		 * Configure a newly created view.
		 * 
		 * @param headerText
		 * @param models
		 * @param isEditabl
		 */
		public void configure(String headerText, List<ColumnModel> models, boolean isEditable);

		/**
		 * Get the current list of ColumnModels.
		 * 
		 * @return
		 */
		public List<ColumnModel> getCurrentModels();
		
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
	}

	/**
	 * Configure a newly created view.
	 * 
	 * @param headerText
	 * @param models
	 * @param isEditabl
	 */
	public void configure(String headerText, List<ColumnModel> models,
			boolean isEditable);

	/**
	 * Get the current list of the ColumnModels in this view. Any changes the
	 * user has made will be reflected in this list.
	 * 
	 * @return
	 */
	public List<ColumnModel> getCurrentModels();

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
	 * Clear any columns from the view.
	 */
	public void clear();

	/**
	 * Show an error.
	 * @param string
	 */
	void showError(String string);

	/**
	 * Add a new Column with the given id.
	 * @param id
	 */
	void addNewColumn(String id);

	/**
	 * Set the name of a column.
	 * @param id
	 * @param name
	 * @param isEditable 
	 */
	void setName(String id, String name, boolean isEditable);

	/**
	 * Set the ColumnType of a view.
	 * @param id
	 * @param columnType
	 * @param isEditable
	 */
	void setColumnType(String id, ColumnType columnType,
			boolean isEditable);

	/**
	 * Set the Column max size.
	 * @param id
	 * @param string
	 * @param b
	 */
	void setColumnMaxSize(String id, String string, boolean b);

	/**
	 * Set column default value.
	 * @param id
	 * @param defaultValue
	 * @param editable
	 */
	void setColumnDefault(String id, String defaultValue,
			boolean editable);
}
