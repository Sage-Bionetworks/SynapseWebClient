package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a <tr> representation of a ColumnModel
 * @author John
 *
 */
public interface ColumnModelTableRow extends IsWidget {
	
	/**
	 * Control for this view.
	 * @author John
	 *
	 */
	public interface SelectionPresenter{
		
		/**
		 * Called when selection changes.
		 */
		public void selectionChanged(boolean isSelected);
	}
	
	/**
	 * ColumnModel.id
	 * @return
	 */
	public String getId();
	
	/**
	 * ColumnModel.id
	 */
	public void setId(String id);

	/**
	 * ColumnModel.name
	 * @return
	 */
	public String getColumnName();
	
	/**
	 * ColumnModel.name
	 */
	public void setColumnName(String name);
	
	/**
	 * ColumnModel.columnType
	 * @return
	 */
	public ColumnTypeViewEnum getColumnType();
	
	/**
	 * ColumnModel.columnType
	 */
	public void setColumnType(ColumnTypeViewEnum type);
	
	/**
	 * ColumnModel.maximumSize
	 * @return
	 */
	public String getMaxSize();
	
	/**
	 * ColumnModel.maximumSize
	 */
	public void setMaxSize(String maxSize);
	
	/**
	 * ColumnModel.defaultValue
	 * @return
	 */
	public String getDefaultValue();
	
	/**
	 * ColumnModel.defaultValue
	 */
	public void setDefaultValue(String defaultValue);
	
	/**
	 * Set the selection selection state of this row
	 * @param select
	 */
	public void setSelected(boolean select);
	
	/**
	 * Is this row selected?
	 */
	public boolean isSelected();

	/**
	 * Delete this row.
	 */
	public void delete();
	
	/**
	 * Set the selection presenter for this view.
	 * @param selectionPresenter
	 */
	public void setSelectionPresenter(SelectionPresenter selectionPresenter);
	
	/**
	 * Restrict values to an enumeration.
	 * 
	 * @param enums
	 */
	public void setEnumValues(List<String> enums);
	
	/**
	 * Restrict values to an enumeration.
	 * @return
	 */
	public List<String> getEnumValues();
}
