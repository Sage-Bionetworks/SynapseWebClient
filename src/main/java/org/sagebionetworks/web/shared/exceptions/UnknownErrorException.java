package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class UnknownErrorException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public UnknownErrorException() {
		super();
	}

	public UnknownErrorException(String message) {
		super(message);
	}

	public UnknownErrorException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
