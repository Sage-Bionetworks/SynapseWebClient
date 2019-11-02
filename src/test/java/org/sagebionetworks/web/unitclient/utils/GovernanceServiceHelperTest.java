package org.sagebionetworks.web.unitclient.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class GovernanceServiceHelperTest {
	SynapseClientAsync mockSynapseClient;
	@Captor
	ArgumentCaptor<AccessApproval> captor;
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
		GovernanceServiceHelper.signTermsOfUse(principalId, touAr, mockSynapseClient, mockCallback);
		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), eq(mockCallback));
		verify(mockCallback).onSuccess(any(AccessApproval.class));
	}


	@Test
	public void testSignPostMessageTermsOfUse() throws Exception {
		final String principalId = "101";
		final Long accessRequirementId = 102L;

		PostMessageContentAccessRequirement ar = new PostMessageContentAccessRequirement();
		ar.setId(accessRequirementId);

		// verify get access requirement text now just returns the url
		String url = "http://postmessage/target";
		ar.setUrl(url);
		assertEquals(url, GovernanceServiceHelper.getAccessRequirementText(ar));

		GovernanceServiceHelper.signTermsOfUse(principalId, ar, mockSynapseClient, mockCallback);
		// also check the captured entity wrapper to verify the approval object
		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), eq(mockCallback));
		verify(mockCallback).onSuccess(any(AccessApproval.class));

		AccessApproval capturedWrapper = captor.getValue();
		assertEquals(accessRequirementId, capturedWrapper.getRequirementId());
	}

	@Test
	public void testLockAccessRequirementText() throws Exception {
		assertEquals(GovernanceServiceHelper.LOCK_ACCESS_REQUIREMENT_TEXT, GovernanceServiceHelper.getAccessRequirementText(new LockAccessRequirement()));
	}

	@Test
	public void testManagedAccessRequirementText() throws Exception {
		assertEquals("", GovernanceServiceHelper.getAccessRequirementText(new ManagedACTAccessRequirement()));
	}

}
