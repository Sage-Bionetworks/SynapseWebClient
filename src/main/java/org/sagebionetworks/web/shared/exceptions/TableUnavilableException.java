package org.sagebionetworks.web.shared.exceptions;

public class TableUnavilableException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public TableUnavilableException() {
		super();
	}

	public TableUnavilableException(String message) {
		super(message);
	}
}
