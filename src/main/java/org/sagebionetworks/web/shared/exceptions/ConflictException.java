package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class ConflictException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public ConflictException() {
		super();
	}

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
