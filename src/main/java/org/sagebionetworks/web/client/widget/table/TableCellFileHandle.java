package org.sagebionetworks.web.client.widget.table;

public class TableCellFileHandle {
	
	private String tableId;
	private String columnId;
	private String rowId;
	private String versionNumber;
	private String fileHandleId;
	
	public TableCellFileHandle(String tableId, String columnId, String rowId,
			String versionNumber, String fileHandleId) {
		super();
		this.tableId = tableId;
		this.columnId = columnId;
		this.rowId = rowId;
		this.versionNumber = versionNumber;
		this.fileHandleId = fileHandleId;
	}

	public String getTableId() {
		return tableId;
	}

	public String getColumnId() {
		return columnId;
	}

	public String getRowId() {
		return rowId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public String getFileHandleId() {
		return fileHandleId;
	}
	
	@Override
	public String toString() {
		return "TableCellFileHandle [tableId=" + tableId + ", columnId="
				+ columnId + ", rowId=" + rowId + ", versionNumber="
				+ versionNumber + ", fileHandleId=" + fileHandleId + "]";
	}

}

