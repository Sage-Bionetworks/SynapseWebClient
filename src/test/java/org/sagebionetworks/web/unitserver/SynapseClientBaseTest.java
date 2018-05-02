package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientBase;
import org.sagebionetworks.web.server.servlet.SynapseProvider;

@RunWith (MockitoJUnitRunner.class)
public class SynapseClientBaseTest {
	
	@Mock
	SynapseProvider mockSynapseProvider;
	
	@Mock
	SynapseClient mockSynapseClient;
	
	SynapseClientBase synapseClientBase;
	
	String publicAuthBaseUrl, repositoryServiceUrl, fileUrl;
	
	@Mock
	ThreadLocal<HttpServletRequest> mockThreadLocal;
	
	@Mock 
	HttpServletRequest mockRequest;
	
	String userIp = "127.0.0.1";
	
	@Before
	public void setUp() throws Exception {
		repositoryServiceUrl = "asdf.com";
		publicAuthBaseUrl = "qwerty.com";
		fileUrl = "zxcvbn.com";
		synapseClientBase = new SynapseClientBase();
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapseClient);
		System.setProperty(StackEndpoints.REPO_ENDPOINT_KEY, repositoryServiceUrl);
		System.setProperty(StackEndpoints.AUTH_ENDPOINT_KEY, publicAuthBaseUrl);
		System.setProperty(StackEndpoints.FILE_ENDPOINT_KEY, fileUrl);
		Whitebox.setInternalState(synapseClientBase, "synapseProvider", mockSynapseProvider);
		Whitebox.setInternalState(synapseClientBase, "perThreadRequest", mockThreadLocal);
		userIp = "127.0.0.1";
		when(mockThreadLocal.get()).thenReturn(mockRequest);
		when(mockRequest.getRemoteAddr()).thenReturn(userIp);
	}

	@Test
	public void testCreateSynapseClient() {
		String sessionToken = "fakeSessionToken";
		SynapseClient createdClient = synapseClientBase.createSynapseClient(sessionToken);
		assertEquals(mockSynapseClient, createdClient);
		verify(mockSynapseClient).setSessionToken(sessionToken);
		verify(mockSynapseClient).setRepositoryEndpoint(repositoryServiceUrl);
		verify(mockSynapseClient).setAuthEndpoint(publicAuthBaseUrl);
		verify(mockSynapseClient).setFileEndpoint(fileUrl);
		verify(mockSynapseClient).appendUserAgent(SynapseClientBase.PORTAL_USER_AGENT);
		verify(mockSynapseClient).setUserIpAddress(userIp);
		
	}
	
	@Test
	public void testNullThreadLocalRequest() {
		when(mockThreadLocal.get()).thenReturn(null);
		String sessionToken = "fakeSessionToken";
		SynapseClient createdClient = synapseClientBase.createSynapseClient(sessionToken);
		verify(mockSynapseClient, never()).setUserIpAddress(userIp);
	}

}
