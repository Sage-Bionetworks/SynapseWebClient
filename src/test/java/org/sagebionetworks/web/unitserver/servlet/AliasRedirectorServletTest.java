package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mockito.stubbing.Answer;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.server.servlet.AliasRedirectorServlet;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.util.tools.shared.Md5Utils;
import com.google.gwt.util.tools.shared.StringUtils;

public class AliasRedirectorServletTest {

	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	AliasRedirectorServlet servlet;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	
	String authBaseUrl = "authbase";
	String repoServiceUrl = "repourl";
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Captor
	ArgumentCaptor<List<String>> stringListCaptor;
	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new AliasRedirectorServlet();
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		
		mockUrlProvider = mock(ServiceUrlProvider.class);
		
		servlet.setServiceUrlProvider(mockUrlProvider);
		servlet.setSynapseProvider(mockSynapseProvider);
		
		// Setup output stream and response
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);

		// Setup request
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
	
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);

		when(mockSynapse.getUserGroupHeadersByAliases(anyList())).thenReturn(Collections.singletonList(mockUserGroupHeader));
		
		//when asking to encode the redirect URL, just return the input param.
		when(mockResponse.encodeRedirectURL(anyString())).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String) invocation.getArguments()[0];
			}
		});
	}
	
	@Test
	public void testDoGetUserAlias() throws Exception {
		//set up general synapse client configuration test
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
		
		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		
		//look for no cache headers
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
		
		//redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}
}	

