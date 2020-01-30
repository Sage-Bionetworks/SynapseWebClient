package org.sagebionetworks.web.client.widget.table.modal.upload;

/**
 * Maps content-type to a delimiter.
 * 
 * @author John
 *
 */
public enum ContentTypeDelimiter {

	TSV("text/tab-separated-values", "\t", "tsv", "tab"), CSV("text/csv", ",", "csv", "txt"), TEXT("text/plain", null);

	String contentType;
	String delimiter;
	String[] extensions;

	/**
	 * 
	 * @param contentType content type string.
	 * @param delimiter Delimiter for this type
	 * @param extentions File name extensions for this type.
	 */
	ContentTypeDelimiter(String contentType, String delimiter, String... extensions) {
		this.contentType = contentType;
		this.delimiter = delimiter;
		this.extensions = extensions;
	}

	public String getContentType() {
		return contentType;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String[] getExtentions() {
		return extensions;
	}

	/**
	 * Find from the content type string.
	 * 
	 * @param type The type is first used. If it is null or unknown, then the file name is used.
	 * @param fileName If the type cannot be determined from the provided type, then the file name is
	 *        used.
	 * @return
	 */
	public static ContentTypeDelimiter findByContentType(String type, String fileName) {
		if (type != null) {
			String lower = type.toLowerCase();
			for (ContentTypeDelimiter ctd : values()) {
				if (ctd.contentType.equals(lower)) {
					return ctd;
				}
			}
		}
		if (fileName != null) {
			// Did not find a match by type so try name.
			fileName = fileName.toLowerCase().trim();
			for (ContentTypeDelimiter ctd : values()) {
				if (ctd.extensions != null) {
					for (String extension : ctd.extensions) {
						if (fileName.endsWith(extension)) {
							return ctd;
						}
					}
				}
			}
		}
		// default to plain text
		return ContentTypeDelimiter.TEXT;
	}
}
