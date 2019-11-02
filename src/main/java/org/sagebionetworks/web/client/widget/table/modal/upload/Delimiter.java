package org.sagebionetworks.web.client.widget.table.modal.upload;

/**
 * Enumerates possible CSV delimiters.
 * 
 * @author jhill
 *
 */
public enum Delimiter {

	CSV(","), TSV("\t"), OTHER(null);

	private String delimiter;

	Delimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Find a delimiter from the given string.
	 * 
	 * @param delimiter
	 * @return
	 */
	public static Delimiter findDelimiter(String delimiter) {
		if (delimiter == null) {
			// default to CSV
			return CSV;
		}
		for (Delimiter del : values()) {
			if (delimiter.equals(del.delimiter)) {
				return del;
			}
		}
		return OTHER;
	}

	public String getDelimiter() {
		return delimiter;
	}
}
