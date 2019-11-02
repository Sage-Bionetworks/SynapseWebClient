package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class DeprecatedServiceException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public DeprecatedServiceException() {
		super();
	}

	public DeprecatedServiceException(String message) {
		super(message);
	}

	public DeprecatedServiceException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
