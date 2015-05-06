package org.sagebionetworks.web.unitclient.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class GovernanceServiceHelperTest {
	SynapseClientAsync mockSynapseClient;
	@Captor ArgumentCaptor<AccessApproval> captor;
	
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
		final Set<String> callbackInvoked = new HashSet<String>(); // simple mutable object to be used in callback
		final Callback onSuccess = new Callback(){
			@Override
			public void invoke() {
				callbackInvoked.add("callback invoked");
			}};
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable param) {
				fail("Exception not expected");
			}};
		final SynapseClientAsync synapseClient = (SynapseClientAsync) Proxy.newProxyInstance(
				GovernanceServiceHelperTest.class.getClassLoader(), 
				new Class<?>[]{SynapseClientAsync.class}, 
				 new InvocationHandler() {
					@Override
					public Object invoke(Object synapseClient, Method method, Object[] args)
							throws Throwable {
						if (method.equals(SynapseClientAsync.class.getMethod("createAccessApproval", AccessApproval.class, AsyncCallback.class))) {
							AccessApproval aa = (AccessApproval)args[0];
			    			assertEquals(accessRequirementId, aa.getRequirementId());
			    			AsyncCallback callback = (AsyncCallback)args[1];
							callback.onSuccess(null);
						}
						return null;
					}
				 }
		);
		
		final JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
		TermsOfUseAccessRequirement touAr = new TermsOfUseAccessRequirement();
		touAr.setId(accessRequirementId);
		GovernanceServiceHelper.signTermsOfUse(principalId,
				touAr,
				onSuccess,
				onFailure,
				synapseClient,
				jsonObjectAdapter
				);
		
		assertFalse(callbackInvoked.isEmpty());
	}
	

	@Test
	public void testSignPostMessageTermsOfUse() throws Exception {
		final String principalId = "101";
		final Long accessRequirementId = 102L;
		Callback onSuccess = mock(Callback.class);
		CallbackP<Throwable> onFailure = mock(CallbackP.class);
		
		PostMessageContentAccessRequirement ar = new PostMessageContentAccessRequirement();
		ar.setId(accessRequirementId);
		JSONObjectAdapterImpl adapter = new JSONObjectAdapterImpl();
		GovernanceServiceHelper.signTermsOfUse(principalId,
				ar,
				onSuccess,
				onFailure,
				mockSynapseClient,
				adapter.createNew()
				);
		verify(onSuccess).invoke();
		//also check the captured entity wrapper to verify the approval object
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
