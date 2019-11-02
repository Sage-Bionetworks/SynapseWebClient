package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class NotFoundException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
