package org.sagebionetworks.web.shared.exceptions;

public class ConflictException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public ConflictException() {
		super();
	}

	public ConflictException(String message) {
		super(message);
	}

}
