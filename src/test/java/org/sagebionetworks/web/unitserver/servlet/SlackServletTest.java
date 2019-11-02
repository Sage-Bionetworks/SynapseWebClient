package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.SlackServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;
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

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new SlackServlet();
		ReflectionTestUtils.setField(servlet, "synapseProvider", mockSynapseProvider);

		when(mockSynapse.getEntityBundleV2(anyString(), any(EntityBundleRequest.class))).thenReturn(mockEntityBundle);
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
		when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);

		Map<String, AnnotationsValue> annotations = new HashMap<String, AnnotationsValue>();
		List<String> values = new ArrayList<String>();
		values.add(ENTITY_STRING_ANNOTATION_VALUE1);
		values.add(ENTITY_STRING_ANNOTATION_VALUE2);
		AnnotationsValue value = new AnnotationsValue();
		value.setType(AnnotationsValueType.STRING);
		value.setValue(values);
		annotations.put(ENTITY_STRING_ANNOTATION_KEY, value);
		when(mockAnnotations.getAnnotations()).thenReturn(annotations);
		SynapseClientBaseTest.setupTestEndpoints();
	}

	@Test
	public void testDoGet() throws Exception {
		String requestSynId = "syn1234";
		when(mockRequest.getParameter("text")).thenReturn(requestSynId);
		when(mockRequest.getParameter("command")).thenReturn("/synapse");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getEntityBundleV2(anyString(), any(EntityBundleRequest.class));

		verify(mockOutputStream).write(byteArrayCaptor.capture(), anyInt(), anyInt());
		String outputValue = new String(byteArrayCaptor.getValue());
		assertTrue(outputValue.contains(ENTITY_NAME));
		assertTrue(outputValue.contains(ENTITY_PROJECT));
		assertTrue(outputValue.contains(THREAD_COUNT.toString()));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_KEY));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_VALUE1));
		assertTrue(outputValue.contains(ENTITY_STRING_ANNOTATION_VALUE2));

		// as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(SynapseClientBaseTest.AUTH_BASE);
		verify(mockSynapse).setRepositoryEndpoint(SynapseClientBaseTest.REPO_BASE);
		verify(mockSynapse).setFileEndpoint(SynapseClientBaseTest.FILE_BASE);
	}

	@Test
	public void testDoGetStaging() throws Exception {
		String requestSynId = "syn1234";
		when(mockRequest.getParameter("text")).thenReturn(requestSynId);
		when(mockRequest.getParameter("command")).thenReturn("/synapsestaging");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getEntityBundleV2(anyString(), any(EntityBundleRequest.class));

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
