package org.sagebionetworks.web.unitshared.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.client.exceptions.SynapseConflictingUpdateException;
import org.sagebionetworks.client.exceptions.SynapseDeprecatedServiceException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServiceUnavailable;
import org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.client.exceptions.UnknownSynapseServerException;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.DeprecatedServiceException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.TooManyRequestsException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

@RunWith(Parameterized.class)
public class ExceptionUtilTest {
	private static String message = "msg";

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {{ForbiddenException.class, new SynapseForbiddenException(message)}, {BadRequestException.class, new SynapseBadRequestException(message)}, {NotFoundException.class, new SynapseNotFoundException(message)}, {UnauthorizedException.class, new SynapseUnauthorizedException(message)}, {UnknownErrorException.class, new SynapseClientException(message)}, {UnknownErrorException.class, new UnknownSynapseServerException(500, message)}, {SynapseDownException.class, new SynapseServiceUnavailable(message)}, {TooManyRequestsException.class, new SynapseTooManyRequestsException(message)}, {ConflictException.class, new UnknownSynapseServerException(409, "Service Error(409):  FAILURE: Got HTTP status 409 for")}, {ConflictingUpdateException.class, new SynapseConflictingUpdateException(message)}, {DeprecatedServiceException.class, new SynapseDeprecatedServiceException(message)}, {BadRequestException.class, new JSONException(message)}});
	}

	Class<? extends RestServiceException> restServiceException;
	Exception synapseException;

	public ExceptionUtilTest(Class<? extends RestServiceException> restServiceException, Exception synapseException) {
		super();
		this.restServiceException = restServiceException;
		this.synapseException = synapseException;
	}

	@Test
	public void testConvertSynapseForbiddenException() {
		RestServiceException ex = ExceptionUtil.convertSynapseException(synapseException);
		assertNotNull(ex);
		assertEquals(restServiceException, ex.getClass());
	}

	@Test
	public void testConvertSynapseClientException() {
		SynapseClientException synapseClientEx = new SynapseClientException(synapseException);
		RestServiceException ex = ExceptionUtil.convertSynapseException(synapseClientEx);
		assertNotNull(ex);
		assertEquals(restServiceException, ex.getClass());
	}
}
