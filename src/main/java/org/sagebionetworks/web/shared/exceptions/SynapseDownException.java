package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class SynapseDownException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public SynapseDownException() {
		super();
	}

	public SynapseDownException(String message) {
		super(message);
	}

	public SynapseDownException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
