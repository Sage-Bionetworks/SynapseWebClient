package org.sagebionetworks.web.shared.exceptions;

public class TermsOfUseException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public TermsOfUseException() {

	}

	public TermsOfUseException(String termsOfUseText) {
		super(termsOfUseText);
	}
}
