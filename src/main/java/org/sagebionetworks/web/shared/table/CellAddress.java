package org.sagebionetworks.web.shared.table;

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
	String columnId;
	Long rowId;
	Long rowVersion;
	
	/**
	 * Create a new address.
	 * @param tableId The ID of the table.
	 * @param rowId The ID of the row.
	 * @param rowVersion The version number of the row.
	 */
	public CellAddress(String tableId, String columnId, Long rowId, Long rowVersion) {
		super();
		this.tableId = tableId;
		this.columnId = columnId;
		this.rowId = rowId;
		this.rowVersion = rowVersion;
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

	public String getColumnId() {
		return columnId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnId == null) ? 0 : columnId.hashCode());
		result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
		result = prime * result
				+ ((rowVersion == null) ? 0 : rowVersion.hashCode());
		result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
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
		if (columnId == null) {
			if (other.columnId != null)
				return false;
		} else if (!columnId.equals(other.columnId))
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
		return true;
	}
	
	/**
	 * A row key is a concatenation of the rowId and rowVerions;
	 * @return
	 */
	public String getRowKey(){
		return this.rowId +"-"+this.rowVersion;
	}

}
