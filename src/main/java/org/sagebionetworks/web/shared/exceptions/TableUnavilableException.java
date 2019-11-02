package org.sagebionetworks.web.shared.exceptions;

public class TableUnavilableException extends RestServiceException {

	private static final long serialVersionUID = 1L;
	private String statusJson;

	public TableUnavilableException() {
		super();
	}

	/**
	 * 
	 * @param statusJson JSON serialized TableStatus object
	 */
	public TableUnavilableException(String statusJson) {
		super(statusJson);
		this.statusJson = statusJson;
	}

	/**
	 * 
	 * @return TableStatus json model string
	 */
	public String getStatusJson() {
		return statusJson;
	}

}
