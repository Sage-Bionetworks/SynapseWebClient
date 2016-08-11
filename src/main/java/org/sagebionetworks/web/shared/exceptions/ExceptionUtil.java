package org.sagebionetworks.web.shared.exceptions;

import org.apache.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseLockedException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;

public class ExceptionUtil {

	static private Log log = LogFactory.getLog(ExceptionUtil.class);

	
	/**
	 * Provides a mapping from Synapse Java client exceptions to their GWT IsSerializable equivalents
	 * @param ex
	 * @return
	 */
	public static RestServiceException convertSynapseException(SynapseException ex) {
		log.error(ex);
		if(ex instanceof SynapseForbiddenException) {			
			return new ForbiddenException(ex.getMessage());
		} else if(ex instanceof SynapseBadRequestException) {
			return new BadRequestException(ex.getMessage());
		} else if(ex instanceof SynapseNotFoundException) {
			return new NotFoundException(ex.getMessage());
		} else if(ex instanceof SynapseUnauthorizedException) {
			return new UnauthorizedException(ex.getMessage());
		} else if(ex instanceof SynapseLockedException) {
			return new LockedException(ex.getMessage());
		} else if (ex instanceof SynapseServerException) {
			SynapseServerException sse = (SynapseServerException)ex;
			if (sse.getStatusCode()==HttpStatus.SC_CONFLICT) {
				return new ConflictException(ex.getMessage());
			} else if (sse.getStatusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE) {
				return new SynapseDownException(ex.getMessage());
			}
		}
		return new UnknownErrorException(ex.getMessage());
	}
}
