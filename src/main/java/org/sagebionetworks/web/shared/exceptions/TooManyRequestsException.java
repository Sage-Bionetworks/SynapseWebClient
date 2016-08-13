package org.sagebionetworks.web.shared.exceptions;

public class TooManyRequestsException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public TooManyRequestsException() {
		super();
	}

	public TooManyRequestsException(String message) {
		super(message);
	}

}
