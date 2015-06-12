package org.sagebionetworks.web.unitclient;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.ClientLoggerImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class ClientLoggerImplTest {

	ClientLoggerImpl clientLogger;
	SynapseClientAsync mockSynapseClient;
	
	@Before
	public void setUp() throws Exception {
		mockSynapseClient = mock(SynapseClientAsync.class);
		clientLogger = new ClientLoggerImpl(mockSynapseClient);
	}

	@Test
	public void testErrorToRepositoryServices() {
		String exceptionMessage = "syn1234";
		ConflictException e = new ConflictException(exceptionMessage);
		String errorMessage = "Unable to upload a file for some specific reason.";
		clientLogger.errorToRepositoryServices(errorMessage, e);
		
		verify(mockSynapseClient).logErrorToRepositoryServices(eq(errorMessage), null, eq(exceptionMessage), eq(e.getStackTrace()), any(AsyncCallback.class));
	}
	

}
