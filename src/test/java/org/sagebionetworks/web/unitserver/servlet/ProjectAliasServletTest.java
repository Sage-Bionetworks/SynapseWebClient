package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ProjectAliasServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class ProjectAliasServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	ProjectAliasServlet servlet;
	String testAliasUrl = "https://www.synapse.org/MyAlias";
	String testAliasSynapseId = "syn3334444";
	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		servlet = new ProjectAliasServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(SynapseClient.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		mockUrlProvider = mock(ServiceUrlProvider.class);
		mockTokenProvider = mock(TokenProvider.class);

		servlet.setServiceUrlProvider(mockUrlProvider);
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
		
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(1L);
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		EntityQueryResult result = new EntityQueryResult();
		result.setId(testAliasSynapseId);
		entities.add(result);
		results.setEntities(entities);

		when(mockSynapse.entityQuery(any(EntityQuery.class))).thenReturn(results);
	}
	

	@Test
	public void testHappyCaseRedirect() throws Exception {
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).entityQuery(any(EntityQuery.class));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).encodeRedirectURL(captor.capture());
		assertTrue(captor.getValue().endsWith("/#!Synapse:"+testAliasSynapseId));
		verify(mockResponse).sendRedirect(anyString());
	}
	
	@Test
	public void testNoAliasMapping() throws Exception {
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(0L);
		results.setEntities(new ArrayList<EntityQueryResult>());
		when(mockSynapse.entityQuery(any(EntityQuery.class))).thenReturn(results);
		
		servlet.doGet(mockRequest, mockResponse);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String value = captor.getValue();
		assertTrue(value.contains("/#!Error:"));
	}
}	

