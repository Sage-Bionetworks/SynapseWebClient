package org.sagebionetworks.web.client.widget.table.modal.download;


/**
 * File type for as CSV download.
 * 
 * @author jhill
 *
 */
public enum FileType {
	CSV(","), TSV("\t");

	private String separator;

	FileType(String separator) {
		this.separator = separator;
	}

	/**
	 * The actual separtor
	 * 
	 * @return
	 */
	public String getSeparator() {
		return this.separator;
	}
}
