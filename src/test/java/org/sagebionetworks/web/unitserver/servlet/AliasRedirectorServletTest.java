package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.AliasRedirectorServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class AliasRedirectorServletTest {

	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	AliasRedirectorServlet servlet;
	@Mock
	UserGroupHeader mockUserGroupHeader;

	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Captor
	ArgumentCaptor<List<String>> stringListCaptor;

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new AliasRedirectorServlet();
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		servlet.setSynapseProvider(mockSynapseProvider);

		// Setup output stream and response
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);

		// Setup request
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");

		when(mockSynapse.getUserGroupHeadersByAliases(anyList())).thenReturn(Collections.singletonList(mockUserGroupHeader));

		// when asking to encode the redirect URL, just return the input param.
		when(mockResponse.encodeRedirectURL(anyString())).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String) invocation.getArguments()[0];
			}
		});
		SynapseClientBaseTest.setupTestEndpoints();
	}

	@Test
	public void testDoGetUserAlias() throws Exception {
		// set up general synapse client configuration test
		String alias = "myAlias";
		String ownerId = "123";
		when(mockRequest.getParameter(WebConstants.ALIAS_PARAM_KEY)).thenReturn(alias);
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(true);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getUserGroupHeadersByAliases(stringListCaptor.capture());
		List<String> aliasList = stringListCaptor.getValue();
		assertEquals(alias, aliasList.get(0));
		verify(mockResponse).sendRedirect(stringCaptor.capture());
		String redirectUrl = stringCaptor.getValue();

		assertTrue(redirectUrl.contains(AliasRedirectorServlet.PROFILE_PLACE));
		assertTrue(redirectUrl.contains(ownerId));

		// as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(SynapseClientBaseTest.AUTH_BASE);
		verify(mockSynapse).setRepositoryEndpoint(SynapseClientBaseTest.REPO_BASE);

		// look for no cache headers
		verify(mockResponse).setHeader(eq(WebConstants.CACHE_CONTROL_KEY), eq(WebConstants.CACHE_CONTROL_VALUE_NO_CACHE));
		verify(mockResponse).setHeader(eq(WebConstants.PRAGMA_KEY), eq(WebConstants.NO_CACHE_VALUE));
		verify(mockResponse).setDateHeader(eq(WebConstants.EXPIRES_KEY), eq(0L));
	}


	@Test
	public void testDoGetTeamAlias() throws Exception {
		String alias = "myTeamAlias";
		String ownerId = "456";
		when(mockRequest.getParameter(WebConstants.ALIAS_PARAM_KEY)).thenReturn(alias);
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(false);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getUserGroupHeadersByAliases(stringListCaptor.capture());
		List<String> aliasList = stringListCaptor.getValue();
		assertEquals(alias, aliasList.get(0));
		verify(mockResponse).sendRedirect(stringCaptor.capture());
		String redirectUrl = stringCaptor.getValue();

		assertTrue(redirectUrl.contains(AliasRedirectorServlet.TEAM_PLACE));
		assertTrue(redirectUrl.contains(ownerId));
	}

	@Test
	public void testDoGetError() throws Exception {
		String errorMessage = "An error from the service call";
		when(mockSynapse.getUserGroupHeadersByAliases(anyList())).thenThrow(new SynapseForbiddenException(errorMessage));
		servlet.doGet(mockRequest, mockResponse);

		// redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}
}

