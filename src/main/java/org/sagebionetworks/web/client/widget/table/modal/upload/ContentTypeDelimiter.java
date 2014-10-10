package org.sagebionetworks.web.client.widget.table.modal.upload;
/**
 * Maps content-type to a delimiter.
 * 
 * @author John
 *
 */
public enum ContentTypeDelimiter {

	TSV("text/tab-separated-values", "\t"),
	CSV("text/csv", ","),
	TEXT("text/plain", null);;
	
	String contentType;
	String delimiter;
	
	ContentTypeDelimiter(String contentType, String delimiter){
		this.contentType = contentType;
		this.delimiter = delimiter;
	}

	public String getContentType() {
		return contentType;
	}

	public String getDelimiter() {
		return delimiter;
	}
	
	/**
	 * Find from the content type string.
	 * 
	 * @param type
	 * @return
	 */
	public static ContentTypeDelimiter findByContentType(String type){
		if(type == null){
			throw new IllegalArgumentException("Type cannot be null");
		}
		String lower = type.toLowerCase();
		for(ContentTypeDelimiter ctd: values()){
			if(ctd.contentType.equals(lower)){
				return ctd;
			}
		}
		throw new IllegalArgumentException("Unknown type: "+type);
	}
}
