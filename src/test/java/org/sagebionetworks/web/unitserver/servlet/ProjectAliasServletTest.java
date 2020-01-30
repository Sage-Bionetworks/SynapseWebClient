package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.EntityId;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ProjectAliasServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;

public class ProjectAliasServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	ProjectAliasServlet servlet;
	String testAlias = "MyAlias";
	String testAliasUrl = "https://www.synapse.org/" + testAlias;
	String testAliasSynapseId = "syn3334444";
	String testFilesPath = "/files";

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		servlet = new ProjectAliasServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(SynapseClient.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		mockTokenProvider = mock(TokenProvider.class);

		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		responseOutputStream = mock(ServletOutputStream.class);
		mockResponse = mock(HttpServletResponse.class);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);

		// Setup request
		mockRequest = mock(HttpServletRequest.class);
		StringBuffer sb = new StringBuffer();
		sb.append(testAliasUrl);
		when(mockRequest.getRequestURL()).thenReturn(sb);
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");

		EntityId id = new EntityId();
		id.setId(testAliasSynapseId);
		when(mockSynapse.getEntityIdByAlias(anyString())).thenReturn(id);
	}


	@Test
	public void testHappyCaseRedirect() throws Exception {
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getEntityIdByAlias(anyString());
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).encodeRedirectURL(captor.capture());
		assertTrue(captor.getValue().endsWith("/#!Synapse:" + testAliasSynapseId));
		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testWithPathRedirect() throws Exception {
		mockRequest = mock(HttpServletRequest.class);
		StringBuffer sb = new StringBuffer();
		sb.append(testAliasUrl);
		sb.append(testFilesPath);
		when(mockRequest.getRequestURL()).thenReturn(sb);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getEntityIdByAlias(eq(testAlias));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).encodeRedirectURL(captor.capture());
		assertTrue(captor.getValue().endsWith("/#!Synapse:" + testAliasSynapseId + testFilesPath));
		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testNoAliasMapping() throws Exception {

		when(mockSynapse.getEntityIdByAlias(anyString())).thenThrow(new SynapseNotFoundException("not found"));

		servlet.doGet(mockRequest, mockResponse);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String value = captor.getValue();
		assertTrue(value.contains("#!Error:"));
	}
}

