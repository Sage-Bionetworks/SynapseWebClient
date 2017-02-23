package org.sagebionetworks.web.unitclient.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class GovernanceServiceHelperTest {
	SynapseClientAsync mockSynapseClient;
	@Captor ArgumentCaptor<AccessApproval> captor;
	@Mock
	AsyncCallback<AccessApproval> mockCallback;
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockSynapseClient = mock(SynapseClientAsync.class);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createAccessApproval(captor.capture(), any(AsyncCallback.class));
	}
	
	
	@Test
	public void testSignTermsOfUse() throws Exception {
		final String principalId = "101";
		final Long accessRequirementId = 102L;
		
		TermsOfUseAccessRequirement touAr = new TermsOfUseAccessRequirement();
		touAr.setId(accessRequirementId);
		GovernanceServiceHelper.signTermsOfUse(principalId,
				touAr,
				mockSynapseClient,
				mockCallback
				);
		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), eq(mockCallback));
		verify(mockCallback).onSuccess(any(AccessApproval.class));
	}
	

	@Test
	public void testSignPostMessageTermsOfUse() throws Exception {
		final String principalId = "101";
		final Long accessRequirementId = 102L;
		
		PostMessageContentAccessRequirement ar = new PostMessageContentAccessRequirement();
		ar.setId(accessRequirementId);
		GovernanceServiceHelper.signTermsOfUse(principalId,
				ar,
				mockSynapseClient,
				mockCallback
				);
		//also check the captured entity wrapper to verify the approval object
		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), eq(mockCallback));
		verify(mockCallback).onSuccess(any(AccessApproval.class));
		
		AccessApproval capturedWrapper = captor.getValue();
		assertEquals(accessRequirementId, capturedWrapper.getRequirementId());
	}
	
	@Test
	public void testSelectAccessRequirement() throws Exception {
		assertEquals(null, GovernanceServiceHelper.selectAccessRequirement(null, null));
		Collection<AccessRequirement> allARs = new ArrayList<AccessRequirement>();
		Collection<AccessRequirement> unmetARs = new ArrayList<AccessRequirement>();
		assertEquals(null, GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs));
		TermsOfUseAccessRequirement tou1 = new TermsOfUseAccessRequirement();
		allARs.add(tou1);
		assertTrue(tou1==GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs));
		TermsOfUseAccessRequirement tou2 = new TermsOfUseAccessRequirement();
		allARs.add(tou2);
		unmetARs.add(tou2);
		assertTrue(tou2==GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs));
	}
	
	@Test
	public void testAccessRequirementApprovalType() throws Exception {
		assertEquals(APPROVAL_TYPE.NONE, 
				GovernanceServiceHelper.accessRequirementApprovalType(null));
		assertEquals(APPROVAL_TYPE.USER_AGREEMENT, 
				GovernanceServiceHelper.accessRequirementApprovalType(new TermsOfUseAccessRequirement()));
		assertEquals(APPROVAL_TYPE.ACT_APPROVAL, 
				GovernanceServiceHelper.accessRequirementApprovalType(new ACTAccessRequirement()));
		assertEquals(APPROVAL_TYPE.POST_MESSAGE, 
				GovernanceServiceHelper.accessRequirementApprovalType(new PostMessageContentAccessRequirement()));
	}
	
	@Test 
	public void testEntityRestrictionLevel() throws Exception {
		assertEquals(RESTRICTION_LEVEL.OPEN, 
				GovernanceServiceHelper.entityRestrictionLevel(null));
		Collection<AccessRequirement> ars = new ArrayList<AccessRequirement>();
		assertEquals(RESTRICTION_LEVEL.OPEN, 
				GovernanceServiceHelper.entityRestrictionLevel(ars));
		ars.add(new TermsOfUseAccessRequirement());
		assertEquals(RESTRICTION_LEVEL.RESTRICTED, 
				GovernanceServiceHelper.entityRestrictionLevel(ars));
		ars.add(new ACTAccessRequirement());
		assertEquals(RESTRICTION_LEVEL.CONTROLLED, 
				GovernanceServiceHelper.entityRestrictionLevel(ars));
		ars.add(new TermsOfUseAccessRequirement());
		assertEquals(RESTRICTION_LEVEL.CONTROLLED, 
				GovernanceServiceHelper.entityRestrictionLevel(ars));
	}
}
