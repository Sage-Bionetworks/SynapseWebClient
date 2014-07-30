package org.sagebionetworks.web.unitclient.widget.table.v2;

import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnTypeViewEnum;

import com.google.gwt.user.client.ui.Widget;

/**
 * Test helper stub of ColumnModelTableRow
 * @author John
 *
 */
public class ColumnModelTableRowStub implements ColumnModelTableRow {
	
	private String id;
	private String columnName;
	private ColumnTypeViewEnum columnType;
	private String maxSize;
	private String defaultValue;
	private boolean isSelected;
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public ColumnTypeViewEnum getColumnType() {
		return columnType;
	}
	public void setColumnType(ColumnTypeViewEnum columnType) {
		this.columnType = columnType;
	}
	public String getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public Widget asWidget() {
		return null;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result
				+ ((columnType == null) ? 0 : columnType.hashCode());
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isSelected ? 1231 : 1237);
		result = prime * result + ((maxSize == null) ? 0 : maxSize.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnModelTableRowStub other = (ColumnModelTableRowStub) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (columnType != other.columnType)
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isSelected != other.isSelected)
			return false;
		if (maxSize == null) {
			if (other.maxSize != null)
				return false;
		} else if (!maxSize.equals(other.maxSize))
			return false;
		return true;
	}
	
}
