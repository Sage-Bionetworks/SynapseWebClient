package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditor;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationAccessControlListEditorTest {

	// The ACLEditor
	private EvaluationAccessControlListEditor acle;

	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory

	// Mock components
	private ChallengeClientAsync mockChallengeClient;
	private AuthenticationController mockAuthenticationController;
	private AccessControlListEditorView mockACLEView;
	private UserAccountServiceAsync mockUserAccountService;
	private Callback mockPushToSynapseCallback;

	// Test Synapse objects
	private static final long OWNER_ID = 1L;
	private static final long ADMIN_ID = 2L;
	private static final long USER_ID = 3L;
	private static final long USER2_ID = 4L;
	private static final Long TEST_PUBLIC_PRINCIPAL_ID = 789l;
	private static final Long TEST_AUTHENTICATED_PRINCIPAL_ID = 123l;
	private static final Long TEST_ANONYMOUS_PRINCIPAL_ID = 422l;
	private static final String EVALUATION_ID = "101";
	private static final String CONTENT_SOURCE = "syn102";
	private static AccessControlList acl;
	private static UserEvaluationPermissions uep;
	private static Evaluation evaluation;
	private static UserGroupHeaderResponsePage userGroupHeaderRP;
	EvaluationAccessControlListEditor.HasChangesHandler mockHasChangesHandler;
	@Mock
	PublicPrincipalIds mockPublicPrincipalIds;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseProperties mockSynapseProperties;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		// set up test Synapse objects
		evaluation = new Evaluation();
		evaluation.setId(EVALUATION_ID);
		evaluation.setContentSource(CONTENT_SOURCE);
		evaluation.setName("Test Evaluation");
		acl = createACL(EVALUATION_ID);
		uep = createUEP();
		userGroupHeaderRP = AccessControlListEditorTest.createUGHRP();
		// set up mocks
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class, RETURNS_DEEP_STUBS);
		mockACLEView = mock(AccessControlListEditorView.class);
		mockUserAccountService = mock(UserAccountServiceAsync.class);

		AsyncMockStubber.callSuccessWith(new PublicPrincipalIds(TEST_PUBLIC_PRINCIPAL_ID, TEST_AUTHENTICATED_PRINCIPAL_ID, TEST_ANONYMOUS_PRINCIPAL_ID)).when(mockUserAccountService).getPublicAndAuthenticatedGroupPrincipalIds(any(AsyncCallback.class));
		when(mockSynapseProperties.getPublicPrincipalIds()).thenReturn(mockPublicPrincipalIds);
		when(mockPublicPrincipalIds.getPublicAclPrincipalId()).thenReturn(TEST_PUBLIC_PRINCIPAL_ID);
		AsyncMockStubber.callSuccessWith(acl.writeToJSONObject(adapterFactory.createNew()).toJSONString()).when(mockChallengeClient).getEvaluationAcl(anyString(), any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith(uep.writeToJSONObject(adapterFactory.createNew()).toJSONString()).when(mockChallengeClient).getUserEvaluationPermissions(anyString(), any(AsyncCallback.class));

		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(new Long(ADMIN_ID).toString());
		AsyncMockStubber.callSuccessWith(userGroupHeaderRP).when(mockSynapseJavascriptClient).getUserGroupHeadersById(Matchers.<ArrayList<String>>any(), any(AsyncCallback.class));

		mockPushToSynapseCallback = mock(Callback.class);

		// instantiate the ACLEditor
		acle = new EvaluationAccessControlListEditor(mockACLEView, mockSynapseJavascriptClient, mockAuthenticationController, mockSynapseProperties, new JSONObjectAdapterImpl(), mockChallengeClient, mockSynAlert);

		mockHasChangesHandler = mock(EvaluationAccessControlListEditor.HasChangesHandler.class);
		acle.configure(evaluation, mockHasChangesHandler);
		acle.refresh();
	}

	private static AccessControlList createACL(String entityId) {
		// create the set of permissions
		Set<ResourceAccess> resourceAccesses = new HashSet<ResourceAccess>();

		// add the owner admin user
		ResourceAccess ownerRA = new ResourceAccess();
		ownerRA.setPrincipalId(OWNER_ID);
		ownerRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER_EVALUATION));
		resourceAccesses.add(ownerRA);

		// add the non-owner admin user
		ResourceAccess adminRA = new ResourceAccess();
		adminRA.setPrincipalId(ADMIN_ID);
		adminRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER_EVALUATION));
		resourceAccesses.add(adminRA);

		// add the non-owner non-admin user
		ResourceAccess userRA = new ResourceAccess();
		userRA.setPrincipalId(USER_ID);
		userRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		resourceAccesses.add(userRA);

		// create the ACL
		AccessControlList acl = new AccessControlList();
		acl.setId(entityId);
		acl.setResourceAccess(resourceAccesses);
		return acl;
	}

	private static UserEvaluationPermissions createUEP() {
		UserEvaluationPermissions uep = new UserEvaluationPermissions();
		uep.setCanChangePermissions(true);
		uep.setCanPublicRead(false);
		uep.setOwnerPrincipalId(OWNER_ID);
		return uep;
	}

	// can't create ACL (acl created for every evaluation)
	// can't delete ACL. No ACL inheritance for evaluation


	@SuppressWarnings("unchecked")
	@Test
	public void addAccessTest() throws Exception {
		// create response ACL: add permissions for USER
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(USER_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		acl.getResourceAccess().add(ra);

		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.asWidget();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockChallengeClient).updateEvaluationAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		acl.setCreationDate(returnedACL.getCreationDate());

		// add/remove public ready, verify it's reflected in UEP
		boolean canPublicRead = acle.getUserEvaluationPermissions().getCanPublicRead();
		assertFalse(canPublicRead);
		acle.setAccess(TEST_PUBLIC_PRINCIPAL_ID, PermissionLevel.CAN_VIEW);
		canPublicRead = acle.getUserEvaluationPermissions().getCanPublicRead();
		assertTrue("setting access to the public principal didn't update the user entity permissions (ACL editor view might be wrong)", canPublicRead);
		acle.removeAccess(TEST_PUBLIC_PRINCIPAL_ID);
		canPublicRead = acle.getUserEvaluationPermissions().getCanPublicRead();
		assertFalse("removing access to the public principal didn't update the user entity permissions (ACL editor view might be wrong)", canPublicRead);

		assertEquals("Updated ACL is invalid", acl, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView).setPublicAclPrincipalId(anyLong());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void changeAccessTest() throws Exception {
		// create response ACL: decrease ADMIN's permissions
		for (ResourceAccess resourceAccess : acl.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				resourceAccess.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));

		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.asWidget();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockChallengeClient).updateEvaluationAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		acl.setCreationDate(returnedACL.getCreationDate());

		Set<ResourceAccess> localRAs = acl.getResourceAccess();
		Set<ResourceAccess> returnedRAs = returnedACL.getResourceAccess();
		assertEquals(returnedRAs, localRAs);

		acl.setResourceAccess(null);
		returnedACL.setResourceAccess(null);
		assertTrue(acl.equals(returnedACL));
		verify(mockACLEView, never()).showErrorMessage(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessTest() throws Exception {
		// create response ACL: remove ADMIN's permissions
		ResourceAccess toRemove = null;
		for (ResourceAccess resourceAccess : acl.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				toRemove = resourceAccess;
		acl.getResourceAccess().remove(toRemove);

		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.asWidget();
		acle.removeAccess(USER_ID);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockChallengeClient).updateEvaluationAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		acl.setCreationDate(returnedACL.getCreationDate());

		assertEquals("Updated ACL is invalid", acl, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
	}


	@SuppressWarnings("unchecked")
	@Test
	public void setAdminAccessTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));

		// update
		acle.asWidget();
		acle.setAccess(ADMIN_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);

		verify(mockSynAlert).showError(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void pushNoChangesTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));

		// attempt to push changes when none have been made
		acle.asWidget();
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessNotFoundTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(acl).when(mockChallengeClient).updateEvaluationAcl(any(AccessControlList.class), any(AsyncCallback.class));

		// attempt to remove permissions for user not on ACL
		acle.asWidget();
		acle.removeAccess(USER2_ID);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);

		verify(mockSynAlert).showError(anyString());
	}
}
