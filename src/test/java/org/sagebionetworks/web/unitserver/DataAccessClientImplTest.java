package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.DataAccessClientImpl;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class DataAccessClientImplTest {
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	SynapseClient mockSynapse;
	DataAccessClientImpl dataAccessClient;
	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		dataAccessClient = new DataAccessClientImpl();
		dataAccessClient.setSynapseProvider(mockSynapseProvider);
		dataAccessClient.setTokenProvider(mockTokenProvider);
		dataAccessClient.setServiceUrlProvider(mockUrlProvider);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
	}
	
//	TODO: add test for your method if it's more than a thin wrapper.
//	@Test
//	public void test() throws RestServiceException, SynapseException {
//	}
}
