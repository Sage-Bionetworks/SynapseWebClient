package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientBase;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.mockito.internal.util.reflection.Whitebox;
import org.junit.runner.RunWith;

@RunWith (MockitoJUnitRunner.class)
public class SynapseClientBaseTest {
	
	@Mock
	SynapseProvider mockSynapseProvider;
	
	@Mock
	ServiceUrlProvider mockServiceUrlProvider;
	
	@Mock
	SynapseClient mockSynapseClient;
	
	SynapseClientBase synapseClientBase;
	
	String publicAuthBaseUrl;
	String repositoryServiceUrl;
	
	@Mock
	ThreadLocal<HttpServletRequest> mockThreadLocal;
	
	@Mock 
	HttpServletRequest mockRequest;
	
	String userIp = "127.0.0.1";
	
	@Before
	public void setUp() throws Exception {
		repositoryServiceUrl = "asdf.com";
		publicAuthBaseUrl = "qwerty.com";
		synapseClientBase = new SynapseClientBase();
		synapseClientBase.setServiceUrlProvider(mockServiceUrlProvider);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapseClient);
		when(mockServiceUrlProvider.getRepositoryServiceUrl()).thenReturn(repositoryServiceUrl);
		when(mockServiceUrlProvider.getPublicAuthBaseUrl()).thenReturn(publicAuthBaseUrl);
		Whitebox.setInternalState(synapseClientBase, "synapseProvider", mockSynapseProvider);
		Whitebox.setInternalState(synapseClientBase, "perThreadRequest", mockThreadLocal);
		userIp = "127.0.0.1";
		when(mockThreadLocal.get()).thenReturn(mockRequest);
		when(mockRequest.getRemoteAddr()).thenReturn(userIp);
	}

	@Test
	public void testCreateSynapseClient() {
		String sessionToken = "fakeSessionToken";
		SynapseClient createdClient = synapseClientBase.createSynapseClient("fakeSessionToken");
		assertEquals(mockSynapseClient, createdClient);
		verify(mockSynapseClient).setSessionToken(sessionToken);
		verify(mockSynapseClient).setRepositoryEndpoint(repositoryServiceUrl);
		verify(mockSynapseClient).setAuthEndpoint(publicAuthBaseUrl);
		verify(mockSynapseClient).setFileEndpoint(StackConfiguration
				.getFileServiceEndpoint());
		verify(mockSynapseClient).appendUserAgent(SynapseClientBase.PORTAL_USER_AGENT);
		verify(mockSynapseClient).setUserIpAddress(userIp);
		
	}

}
