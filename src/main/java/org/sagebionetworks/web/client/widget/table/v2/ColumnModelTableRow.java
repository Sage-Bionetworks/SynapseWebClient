package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a <tr> representation of a ColumnModel
 * @author John
 *
 */
public interface ColumnModelTableRow extends IsWidget {
	
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
}
