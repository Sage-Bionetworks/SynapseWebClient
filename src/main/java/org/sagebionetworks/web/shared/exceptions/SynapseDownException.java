package org.sagebionetworks.web.shared.exceptions;

public class SynapseDownException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public SynapseDownException() {
		super();
	}

	public SynapseDownException(String message) {
		super(message);
	}

}
