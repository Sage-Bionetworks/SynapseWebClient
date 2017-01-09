package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SlackServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class SlackServletTest {
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
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	Entity mockEntity;
	SlackServlet servlet;
	public static final String ENTITY_NAME = "file.png";
	public static final String ENTITY_PROJECT = "A Project";
	public static final Long THREAD_COUNT = 42L;
	@Mock
	EntityPath mockEntityPath;
	@Mock
	EntityHeader mockRoot;
	@Mock
	EntityHeader mockParentProject;
	@Mock
	EntityHeader mockCurrentItem;
	
	@Mock
	PrintWriter mockPrintWriter;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	List<EntityHeader> entityPath;
	
	String authBaseUrl = "authbase";
	String repoServiceUrl = "repourl";

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new SlackServlet();
		servlet.setServiceUrlProvider(mockUrlProvider);
		ReflectionTestUtils.setField(servlet, "synapseProvider", mockSynapseProvider);
		
		when(mockSynapse.getEntityBundle(anyString(), anyInt())).thenReturn(mockEntityBundle);
		when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getName()).thenReturn(ENTITY_NAME);
		entityPath = new ArrayList<EntityHeader>();
		entityPath.add(mockRoot);
		entityPath.add(mockParentProject);
		entityPath.add(mockCurrentItem);
		when(mockEntityPath.getPath()).thenReturn(entityPath);
		when(mockEntityBundle.getPath()).thenReturn(mockEntityPath);
		when(mockParentProject.getName()).thenReturn(ENTITY_PROJECT);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
		when(mockEntityBundle.getThreadCount()).thenReturn(THREAD_COUNT);
		//set up general synapse client configuration test
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
	}
	
	@Test
	public void testDoGet() throws Exception {
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
	
		String requestSynId = "syn1234";
		when(mockRequest.getParameter("text")).thenReturn(requestSynId);
		when(mockRequest.getParameter("command")).thenReturn("/synapse");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getEntityBundle(anyString(), anyInt());
		
		verify(mockPrintWriter).println(stringCaptor.capture());
		String outputValue = stringCaptor.getValue();
		assertTrue(outputValue.contains(ENTITY_NAME));
		assertTrue(outputValue.contains(ENTITY_PROJECT));
		assertTrue(outputValue.contains(THREAD_COUNT.toString()));
		
		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		verify(mockSynapse).setFileEndpoint(anyString());
	}


	@Test
	public void testDoGetError() throws Exception {
		when(mockRequest.getParameter("text")).thenReturn("syn99");
		when(mockRequest.getParameter("command")).thenReturn("/invalidcommand");
		
		servlet.doGet(mockRequest, mockResponse);
		verify(mockPrintWriter).println(stringCaptor.capture());
		String outputValue = stringCaptor.getValue();
		assertTrue(outputValue.contains(SlackServlet.INVALID_COMMAND_MESSAGE));
		verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

}
