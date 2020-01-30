package org.sagebionetworks.web.shared.table;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

/**
 * The full address of a cell.
 * 
 * This object is immutable.
 * 
 * @author John
 *
 */
public class CellAddress {

	String tableId;
	ColumnModel column;
	Long rowId;
	Long rowVersion;
	TableType tableType;

	/**
	 * Create a new address.
	 * 
	 * @param tableId The ID of the table.
	 * @param rowId The ID of the row.
	 * @param rowVersion The version number of the row.
	 */
	public CellAddress(String tableId, ColumnModel column, Long rowId, Long rowVersion, TableType tableType) {
		super();
		this.tableId = tableId;
		this.column = column;
		this.rowId = rowId;
		this.rowVersion = rowVersion;
		this.tableType = tableType;
	}

	public String getTableId() {
		return tableId;
	}

	public Long getRowId() {
		return rowId;
	}

	public Long getRowVersion() {
		return rowVersion;
	}

	public ColumnModel getColumn() {
		return column;
	}

	public TableType getTableType() {
		return tableType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
		result = prime * result + ((rowVersion == null) ? 0 : rowVersion.hashCode());
		result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
		result = prime * result + ((tableType == null) ? 0 : tableType.hashCode());
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
		CellAddress other = (CellAddress) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (rowId == null) {
			if (other.rowId != null)
				return false;
		} else if (!rowId.equals(other.rowId))
			return false;
		if (rowVersion == null) {
			if (other.rowVersion != null)
				return false;
		} else if (!rowVersion.equals(other.rowVersion))
			return false;
		if (tableId == null) {
			if (other.tableId != null)
				return false;
		} else if (!tableId.equals(other.tableId))
			return false;
		if (tableType != other.tableType)
			return false;
		return true;
	}

	/**
	 * A row key is a concatenation of the rowId and rowVerions;
	 * 
	 * @return
	 */
	public String getRowKey() {
		return this.rowId + "-" + this.rowVersion;
	}

}
