package org.sagebionetworks.web.unitserver;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptorResponse;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.dataaccess.CreateSubmissionRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.DataAccessClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class DataAccessClientImplTest {
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	SynapseClient mockSynapse;
	@Mock
	CreateSubmissionRequest mockCreateSubmissionRequest;
	@Mock
	RestrictableObjectDescriptorResponse mockRestrictableObjectDescriptorResponse;
	@Mock
	RestrictableObjectDescriptor mockSubject;

	DataAccessClientImpl dataAccessClient;
	public static final Long AR_ID = 9999L;
	public static final String TARGET_SUBJECT_ID = "2";
	public static final RestrictableObjectType TARGET_SUBJECT_TYPE = RestrictableObjectType.ENTITY;

	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		dataAccessClient = new DataAccessClientImpl();
		dataAccessClient.setSynapseProvider(mockSynapseProvider);
		dataAccessClient.setTokenProvider(mockTokenProvider);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		when(mockSubject.getId()).thenReturn(TARGET_SUBJECT_ID);
		when(mockSubject.getType()).thenReturn(TARGET_SUBJECT_TYPE);
		when(mockRestrictableObjectDescriptorResponse.getSubjects()).thenReturn(java.util.Collections.singletonList(mockSubject));
		when(mockSynapse.getSubjects(anyString(), anyString())).thenReturn(mockRestrictableObjectDescriptorResponse);
	}

	@Test
	public void testSubmitDataAccessRequest() throws RestServiceException, SynapseException {
		// if the subject is set, then it should simply submit the request.
		when(mockCreateSubmissionRequest.getSubjectId()).thenReturn("4");
		when(mockCreateSubmissionRequest.getSubjectType()).thenReturn(RestrictableObjectType.TEAM);
		dataAccessClient.submitDataAccessRequest(mockCreateSubmissionRequest, AR_ID);
		verify(mockSynapse).submitRequest(mockCreateSubmissionRequest);
		verify(mockSynapse, never()).getSubjects(anyString(), anyString());
	}

	@Test
	public void testSubmitDataAccessRequestNoTargetSubject() throws RestServiceException, SynapseException {
		// if the subject is not set, then it should associate to a random subject
		dataAccessClient.submitDataAccessRequest(mockCreateSubmissionRequest, AR_ID);
		verify(mockSynapse).getSubjects(AR_ID.toString(), null);

		verify(mockCreateSubmissionRequest).setSubjectId(TARGET_SUBJECT_ID);
		verify(mockCreateSubmissionRequest).setSubjectType(TARGET_SUBJECT_TYPE);
		verify(mockSynapse).submitRequest(mockCreateSubmissionRequest);
	}
}
