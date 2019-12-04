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
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientBase;
import org.sagebionetworks.web.server.servlet.SynapseProvider;

@RunWith(MockitoJUnitRunner.class)
public class SynapseClientBaseTest {

	@Mock
	SynapseProvider mockSynapseProvider;

	@Mock
	SynapseClient mockSynapseClient;

	SynapseClientBase synapseClientBase;

	@Mock
	ThreadLocal<HttpServletRequest> mockThreadLocal;

	@Mock
	HttpServletRequest mockRequest;

	String userIp = "127.0.0.1";
	public static final String ENDPOINT_PREFIX = "endpointprefix";
	public static final String FILE_BASE = ENDPOINT_PREFIX + "/file/v1";
	public static final String AUTH_BASE = ENDPOINT_PREFIX + "/auth/v1";
	public static final String REPO_BASE = ENDPOINT_PREFIX + "/repo/v1";

	public static void setupTestEndpoints() {
		System.setProperty(StackEndpoints.REPO_ENDPOINT_KEY, REPO_BASE);
		System.setProperty(StackEndpoints.AUTH_ENDPOINT_KEY, AUTH_BASE);
		System.setProperty(StackEndpoints.FILE_ENDPOINT_KEY, FILE_BASE);
		StackEndpoints.clear();
		StackEndpoints.skipLoadingSettingsFile();
	}

	@Before
	public void setUp() throws Exception {
		synapseClientBase = new SynapseClientBase();
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapseClient);
		Whitebox.setInternalState(synapseClientBase, "synapseProvider", mockSynapseProvider);
		Whitebox.setInternalState(synapseClientBase, "perThreadRequest", mockThreadLocal);
		userIp = "127.0.0.1";
		when(mockThreadLocal.get()).thenReturn(mockRequest);
		when(mockRequest.getRemoteAddr()).thenReturn(userIp);
		SynapseClientBaseTest.setupTestEndpoints();
	}

	@Test
	public void testCreateSynapseClient() {
		String sessionToken = "fakeSessionToken";
		SynapseClient createdClient = synapseClientBase.createSynapseClient(sessionToken);
		assertEquals(mockSynapseClient, createdClient);
		verify(mockSynapseClient).setSessionToken(sessionToken);
		verify(mockSynapseClient).setRepositoryEndpoint(REPO_BASE);
		verify(mockSynapseClient).setAuthEndpoint(AUTH_BASE);
		verify(mockSynapseClient).setFileEndpoint(FILE_BASE);
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

	@Test
	public void testGetIpAddressRequestHasHeader() {
		String otherIp = "42.42.42.42";
		when(mockRequest.getHeader(SynapseClientBase.X_FORWARDED_FOR_HEADER)).thenReturn(otherIp);
		assertEquals(otherIp, SynapseClientBase.getIpAddress(mockRequest));
	}

	@Test
	public void testGetIpAddressRequestDoesNotHaveHeader() {
		when(mockRequest.getHeader(SynapseClientBase.X_FORWARDED_FOR_HEADER)).thenReturn(null);
		assertEquals(userIp, SynapseClientBase.getIpAddress(mockRequest));
	}

}
