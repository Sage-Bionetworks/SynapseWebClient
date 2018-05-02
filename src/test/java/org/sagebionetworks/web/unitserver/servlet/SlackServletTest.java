package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
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
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SlackServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class SlackServletTest {
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
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
	public static final String ENTITY_STRING_ANNOTATION_KEY = "a string annotation key";
	public static final String ENTITY_STRING_ANNOTATION_VALUE1 = "value1";
	public static final String ENTITY_STRING_ANNOTATION_VALUE2 = "value2";
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
	ServletOutputStream mockOutputStream;
	@Mock
	Annotations mockAnnotations;
	@Captor
	ArgumentCaptor<byte[]> byteArrayCaptor;
	List<EntityHeader> entityPath;
	
	String authBaseUrl = "authbase";
	String repoServiceUrl = "repourl";

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new SlackServlet();
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
		when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);
		when(mockEntityBundle.getThreadCount()).thenReturn(THREAD_COUNT);
		//set up general synapse client configuration test
		System.setProperty(StackEndpoints.REPO_ENDPOINT_KEY, repoServiceUrl);
		System.setProperty(StackEndpoints.AUTH_ENDPOINT_KEY, authBaseUrl);
		when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);
		Map<String, List<String>> stringAnnotations = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		values.add(ENTITY_STRING_ANNOTATION_VALUE1);
		values.add(ENTITY_STRING_ANNOTATION_VALUE2);
		stringAnnotations.put(ENTITY_STRING_ANNOTATION_KEY, values);
		when(mockAnnotations.getStringAnnotations()).thenReturn(stringAnnotations);
	}
	
	@Test
	public void testDoGet() throws Exception {
		String requestSynId = "syn1234";
		when(mockRequest.getParameter("text")).thenReturn(requestSynId);
		when(mockRequest.getParameter("command")).thenReturn("/synapse");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getEntityBundle(anyString(), anyInt());
		
		verify(mockOutputStream).write(byteArrayCaptor.capture(), anyInt(), anyInt());
		String outputValue = new String(byteArrayCaptor.getValue());
		assertTrue(outputValue.contains(ENTITY_NAME));
		assertTrue(outputValue.contains(ENTITY_PROJECT));
		assertTrue(outputValue.contains(THREAD_COUNT.toString()));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_KEY));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_VALUE1));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_VALUE2));
		
		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		verify(mockSynapse).setFileEndpoint(anyString());
	}
	
	@Test
	public void testDoGetStaging() throws Exception {
		String requestSynId = "syn1234";
		when(mockRequest.getParameter("text")).thenReturn(requestSynId);
		when(mockRequest.getParameter("command")).thenReturn("/synapsestaging");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getEntityBundle(anyString(), anyInt());
		
		verify(mockOutputStream).write(byteArrayCaptor.capture(), anyInt(), anyInt());
		String outputValue = new String(byteArrayCaptor.getValue());
		assertTrue(outputValue.contains(ENTITY_NAME));
		assertTrue(outputValue.contains(ENTITY_PROJECT));
	}
	
	@Test
	public void testDoGetError() throws Exception {
		when(mockRequest.getParameter("text")).thenReturn("syn99");
		when(mockRequest.getParameter("command")).thenReturn("/invalidcommand");
		
		servlet.doGet(mockRequest, mockResponse);
		verify(mockOutputStream).write(byteArrayCaptor.capture(), anyInt(), anyInt());
		String outputValue = new String(byteArrayCaptor.getValue());
		assertTrue(outputValue.contains(SlackServlet.INVALID_COMMAND_MESSAGE));
		verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	@Test
	public void testDoGetInvalidSynIdError() throws Exception {
		when(mockRequest.getParameter("text")).thenReturn("syn99invalid");
		when(mockRequest.getParameter("command")).thenReturn("/synapse");
		
		servlet.doGet(mockRequest, mockResponse);
		verify(mockOutputStream).write(byteArrayCaptor.capture(), anyInt(), anyInt());
		String outputValue = new String(byteArrayCaptor.getValue());
		assertTrue(outputValue.contains(SlackServlet.IS_INVALID_SYN_ID));
		verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	
	@Test
	public void testJoin() {
		List list = new ArrayList<String>();
		assertEquals("", SlackServlet.join(list));
		list.add("a");
		assertEquals("a", SlackServlet.join(list));
		list.add("b");
		assertEquals("a, b", SlackServlet.join(list));
		
		list = new ArrayList<Integer>();
		assertEquals("", SlackServlet.join(list));
		list.add(1);
		assertEquals("1", SlackServlet.join(list));
		list.add(2);
		assertEquals("1, 2", SlackServlet.join(list));
	}

}
