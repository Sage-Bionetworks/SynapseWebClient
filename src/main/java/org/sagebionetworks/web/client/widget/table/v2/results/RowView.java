package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a table row.
 * 
 * @author John
 * 
 */
public interface RowView extends IsWidget {
	
	/**
	 * Business logic for this view.
	 */
	public interface Presenter {
		/**
		 * When the select button becomes selected.
		 */
		public void onSelected(boolean isSelected);
	}

	/**
	 * The ID of a row that already exists in the table. This will be null for
	 * new row that have not be saved to the table yet.
	 * 
	 * @return
	 */
	public Long getID();

	/**
	 * The ID of a row that already exists in the table. This will be null for
	 * new row that have not be saved to the table yet.
	 * 
	 * @param id
	 */
	public void setId(Long id);

	/**
	 * The version number of a row that already exists in the table. This will
	 * be null for new row that have not be saved to the table yet.
	 * 
	 * @return
	 */
	public Long getVersion();

	/**
	 * Iterate over the cells of a this row.
	 * @return
	 */
	public Iterable<String> getValues();
	
	/**
	 * Is this row selected.
	 * @return
	 */
	public boolean isSelected();
	
	/**
	 * Is this row selected?
	 */
	public void setSelected(boolean isSelected);
	
	/**
	 * Toggle the editing state of this row.
	 * 
	 * @param isEditing
	 */
	public void setEditing(boolean isEditing);
	
	/**
	 * Is this cell currently editing?
	 * @return
	 */
	public boolean isEditing();
	
	/**
	 * Initialize a row with the column type information.
	 * @param types
	 */
	public void initializeRow(List<ColumnTypeViewEnum> types);
	
	/**
	 * Set the data for this row.
	 * @param rowId
	 * @param version
	 * @param cellValues
	 */
	public void setRowData(Long rowId, Long version, List<String> cellValues, boolean isSelectable);
	
	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	

}
