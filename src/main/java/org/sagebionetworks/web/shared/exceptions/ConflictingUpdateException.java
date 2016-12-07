package org.sagebionetworks.web.shared.exceptions;

public class ConflictingUpdateException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public ConflictingUpdateException() {
		super();
	}

	public ConflictingUpdateException(String message) {
		super(message);
	}

}
