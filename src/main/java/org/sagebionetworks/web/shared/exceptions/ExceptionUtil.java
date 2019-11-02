package org.sagebionetworks.web.shared.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.client.exceptions.SynapseConflictingUpdateException;
import org.sagebionetworks.client.exceptions.SynapseDeprecatedServiceException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseLockedException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseResultNotReadyException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.client.exceptions.SynapseServiceUnavailable;
import org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.client.exceptions.UnknownSynapseServerException;
import org.sagebionetworks.repo.model.ErrorResponseCode;

public class ExceptionUtil {

	static private Log log = LogFactory.getLog(ExceptionUtil.class);


	/**
	 * Provides a mapping from Synapse Java client exceptions to their GWT IsSerializable equivalents
	 * 
	 * @param ex
	 * @return
	 */
	public static RestServiceException convertSynapseException(Throwable ex) {
		log.error(ex);
		if (ex instanceof SynapseClientException && ex.getCause() != null) {
			ex = ex.getCause();
		}
		ErrorResponseCode code = null;
		if (ex instanceof SynapseServerException) {
			code = ((SynapseServerException) ex).getErrorResponseCode();
		}
		if (ex instanceof SynapseForbiddenException) {
			return new ForbiddenException(ex.getMessage(), code);
		} else if (ex instanceof SynapseBadRequestException) {
			return new BadRequestException(ex.getMessage(), code);
		} else if (ex instanceof SynapseNotFoundException) {
			return new NotFoundException(ex.getMessage(), code);
		} else if (ex instanceof SynapseUnauthorizedException) {
			return new UnauthorizedException(ex.getMessage(), code);
		} else if (ex instanceof SynapseLockedException) {
			return new LockedException(ex.getMessage(), code);
		} else if (ex instanceof SynapseTooManyRequestsException) {
			return new TooManyRequestsException(ex.getMessage(), code);
		} else if (ex instanceof SynapseConflictingUpdateException) {
			return new ConflictingUpdateException(ex.getMessage(), code);
		} else if (ex instanceof SynapseResultNotReadyException) {
			return new ResultNotReadyException(((SynapseResultNotReadyException) ex).getJobStatus());
		} else if (ex instanceof SynapseServiceUnavailable) {
			return new SynapseDownException(ex.getMessage(), code);
		} else if (ex instanceof SynapseDeprecatedServiceException) {
			return new DeprecatedServiceException(ex.getMessage(), code);
		} else if (ex instanceof UnknownSynapseServerException) {
			UnknownSynapseServerException sse = (UnknownSynapseServerException) ex;
			if (sse.getStatusCode() == HttpStatus.SC_CONFLICT) {
				return new ConflictException(ex.getMessage(), code);
			}
		} else if (ex instanceof JSONException) {
			return new BadRequestException("The Synapse web client is calling a Synapse backend service that's not available!", code);
		}
		return new UnknownErrorException(ex.getMessage(), code);
	}
}
