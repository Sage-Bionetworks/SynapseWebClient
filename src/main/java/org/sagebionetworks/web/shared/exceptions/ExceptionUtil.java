package org.sagebionetworks.web.shared.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServiceException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.client.exceptions.SynapseUserException;

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
		} else if(ex instanceof SynapseUserException && ex.getMessage().contains("(409)")) {
			return new ConflictException(ex.getMessage());
		} else if(ex instanceof SynapseServiceException) {
			if(ex.getMessage().contains("READ_ONLY")) return new ReadOnlyModeException(ex.getMessage());
			else if(ex.getMessage().contains("Synapse is down")) return new SynapseDownException(ex.getMessage());
			else return new UnknownErrorException(ex.getMessage());
		} else { 
			return new UnknownErrorException(ex.getMessage());
		}
	}
}
