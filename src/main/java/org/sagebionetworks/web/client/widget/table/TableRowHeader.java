package org.sagebionetworks.web.client.widget.table;

public class TableRowHeader {
	
	private String rowId;
	private String version;
	
	public TableRowHeader() {
		
	}
	
	public TableRowHeader(String rowId, String version) {
		super();
		this.rowId = rowId;
		this.version = version;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
