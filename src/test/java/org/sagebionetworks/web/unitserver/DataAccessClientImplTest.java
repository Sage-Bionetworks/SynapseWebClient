package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.DataAccessClientImpl;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class DataAccessClientImplTest {
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	SynapseClient mockSynapse;
	@Mock
	AccessRequirementStatus mockAccessRequirementStatus;
	DataAccessClientImpl dataAccessClient;
	public static final String AR_ID1 = "8765";
	public static final String AR_ID2 = "5678";
	public static final boolean IS_APPROVED = false;
	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		dataAccessClient = new DataAccessClientImpl();
		dataAccessClient.setSynapseProvider(mockSynapseProvider);
		dataAccessClient.setTokenProvider(mockTokenProvider);
		dataAccessClient.setServiceUrlProvider(mockUrlProvider);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		when(mockSynapse.getAccessRequirementStatus(anyString())).thenReturn(mockAccessRequirementStatus);
		when(mockAccessRequirementStatus.getIsApproved()).thenReturn(IS_APPROVED);
	}
	
	@Test
	public void testGetAccessRequirementStatus() throws RestServiceException, SynapseException {
		List<String> accessRequirementIds = new ArrayList<String>();
		accessRequirementIds.add(AR_ID1);
		accessRequirementIds.add(AR_ID2);
		List<Boolean> response = dataAccessClient.getAccessRequirementStatus(accessRequirementIds);
		verify(mockSynapse).getAccessRequirementStatus(AR_ID1);
		verify(mockSynapse).getAccessRequirementStatus(AR_ID1);
		for (Boolean isApproved : response) {
			assertEquals(IS_APPROVED, isApproved);
		}
	}
}
