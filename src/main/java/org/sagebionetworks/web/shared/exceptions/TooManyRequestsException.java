package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class TooManyRequestsException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public TooManyRequestsException() {
		super();
	}

	public TooManyRequestsException(String message) {
		super(message);
	}

	public TooManyRequestsException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
