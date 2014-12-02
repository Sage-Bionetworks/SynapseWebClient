package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
		
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).logErrorToRepositoryServices(messageCaptor.capture(), labelCaptor.capture(), any(AsyncCallback.class));
		String message = messageCaptor.getValue();
		String label = labelCaptor.getValue();
		
		//the label specific error info (found in the stack trace message)
		assertFalse(label.contains(exceptionMessage));
		//the message should contain the error message and stack trace message
		assertTrue(message.contains(exceptionMessage));
		assertTrue(message.contains(errorMessage));
		assertTrue(label.contains(ConflictException.class.getName()));
	}
	
	@Test
	public void testErrorToRepositoryServicesNullException() {
		String errorMessage = "Unable to upload a file for another specific reason.";
		clientLogger.errorToRepositoryServices(errorMessage, null);
		
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).logErrorToRepositoryServices(messageCaptor.capture(), labelCaptor.capture(), any(AsyncCallback.class));
		String message = messageCaptor.getValue();
		String label = labelCaptor.getValue();
		
		//the label should be empty
		assertTrue(label != null && label.length() == 0);
		//message should contain the error message
		assertTrue(message.contains(errorMessage));
	}

}
