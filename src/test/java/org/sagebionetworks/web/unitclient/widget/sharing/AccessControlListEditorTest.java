package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessControlListEditorTest {
	
	// The ACLEditor
	private AccessControlListEditor acle;
	
	// JSON utility components
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);

	// Mock components
	private SynapseClientAsync mockSynapseClient;
	private GWTWrapper mockGwt;
	private AuthenticationController mockAuthenticationController;
	private AccessControlListEditorView mockACLEView;
	private UserAccountServiceAsync mockUserAccountService;
	private AsyncCallback<AccessControlList> mockPushToSynapseCallback;
	
	// Test Synapse objects
	private static final long OWNER_ID = 1L;
	private static final long ADMIN_ID = 2L;
	private static final long USER_ID = 3L;
	private static final long USER2_ID = 4L;
	private static final long TEAM_ID = 5L;
	private static final Long TEST_PUBLIC_PRINCIPAL_ID = 789l;
	private static final Long TEST_AUTHENTICATED_PRINCIPAL_ID = 123l;
	private static final Long TEST_ANONYMOUS_USER_PRINCIPAL_ID = 422l;
	private static final String OWNER_NAME = "Owner";
	private static final String ENTITY_ID = "syn101";
	private static final String INHERITED_ACL_ID = "syn202";
	private static AccessControlList localACL;
	private static AccessControlList inheritedACL;
	private static EntityBundleTransport entityBundleTransport_localACL;
	private static EntityBundleTransport entityBundleTransport_inheritedACL;
	private static Project project;
	private static UserGroupHeaderResponsePage userGroupHeaderRP;
	GlobalApplicationState mockGlobalApplicationState;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JSONObjectAdapterException {
		// set up test Synapse objects
		project = createProject();
		localACL = createACL(ENTITY_ID);
		inheritedACL = createACL(INHERITED_ACL_ID);
		entityBundleTransport_localACL = createEBT(localACL, createUEP());
		entityBundleTransport_inheritedACL = createEBT(inheritedACL, createUEP());
		userGroupHeaderRP = createUGHRP();
		
		
		// set up mocks
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class, RETURNS_DEEP_STUBS);
		mockACLEView = mock(AccessControlListEditorView.class);
		mockUserAccountService = mock(UserAccountServiceAsync.class);
		AsyncMockStubber.callSuccessWith(new PublicPrincipalIds(TEST_PUBLIC_PRINCIPAL_ID, TEST_AUTHENTICATED_PRINCIPAL_ID,TEST_ANONYMOUS_USER_PRINCIPAL_ID)).when(mockUserAccountService).getPublicAndAuthenticatedGroupPrincipalIds(any(AsyncCallback.class));
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockGwt = mock(GWTWrapper.class);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(new Long(ADMIN_ID).toString());
		AsyncMockStubber.callSuccessWith(userGroupHeaderRP).when(mockSynapseClient).getUserGroupHeadersById(Matchers.<ArrayList<String>>any(), any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
		
		mockPushToSynapseCallback = mock(AsyncCallback.class);
		
		// instantiate the ACLEditor
		acle = new AccessControlListEditor(mockACLEView,
				mockSynapseClient,
				nodeModelCreator,
				mockAuthenticationController,
				new JSONObjectAdapterImpl(),
				mockUserAccountService,
				mockGlobalApplicationState, 
				mockGwt,
				adapterFactory
		);
		acle.setResource(project, true);
		when(mockACLEView.isNotifyPeople()).thenReturn(true);
	}
	
	private static Project createProject() {
		Project p = new Project();
		p.setId(ENTITY_ID);
		p.setCreatedBy(OWNER_NAME);
		return p;
	}
	
	private static EntityBundleTransport createEBT(AccessControlList acl, UserEntityPermissions uep) {
		try {
			EntityBundleTransport ebt = new EntityBundleTransport();
			ebt.setAclJson(EntityFactory.createJSONStringForEntity(acl));
			ebt.setPermissionsJson(EntityFactory.createJSONStringForEntity(uep));
			return ebt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static AccessControlList createACL(String entityId) {
		// create the set of permissions
		Set<ResourceAccess> resourceAccesses = new HashSet<ResourceAccess>();
		
		// add the owner admin user
		ResourceAccess ownerRA = new ResourceAccess();
		ownerRA.setPrincipalId(OWNER_ID);
		ownerRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
		resourceAccesses.add(ownerRA);
		
		// add the non-owner admin user
		ResourceAccess adminRA = new ResourceAccess();
		adminRA.setPrincipalId(ADMIN_ID);
		adminRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
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
	
	private static UserEntityPermissions createUEP() {
		UserEntityPermissions uep = new UserEntityPermissions();
		uep = new UserEntityPermissions();
		uep.setCanChangePermissions(true);
		uep.setCanEnableInheritance(true);
		uep.setCanPublicRead(false);
		uep.setOwnerPrincipalId(OWNER_ID);
		return uep;
	}

	public static UserGroupHeaderResponsePage createUGHRP() {
		UserGroupHeaderResponsePage ughrp = new UserGroupHeaderResponsePage();		
		List<UserGroupHeader> children = new ArrayList<UserGroupHeader>();

		// add the owner admin user
		UserGroupHeader ownerHeader = new UserGroupHeader();
		ownerHeader.setOwnerId(new Long(OWNER_ID).toString());
		ownerHeader.setIsIndividual(true);
		children.add(ownerHeader);
		
		// add the non-owner admin user
		UserGroupHeader adminHeader = new UserGroupHeader();
		adminHeader.setOwnerId(new Long(ADMIN_ID).toString());
		adminHeader.setIsIndividual(true);
		children.add(adminHeader);
		
		// add the non-owner non-admin user
		UserGroupHeader userHeader = new UserGroupHeader();
		userHeader.setOwnerId(new Long(USER_ID).toString());
		userHeader.setIsIndividual(true);
		children.add(userHeader);
		
		// add the non-owner non-admin user
		UserGroupHeader user2Header = new UserGroupHeader();
		user2Header.setOwnerId(new Long(USER2_ID).toString());
		user2Header.setIsIndividual(true);
		children.add(user2Header);
		
		// add a team
		UserGroupHeader teamHeader = new UserGroupHeader();
		teamHeader.setOwnerId(new Long(TEAM_ID).toString());
		teamHeader.setIsIndividual(false);
		children.add(teamHeader);
				
		
		// add the public group
		UserGroupHeader publicHeader = new UserGroupHeader();
		publicHeader.setOwnerId(new Long(TEST_PUBLIC_PRINCIPAL_ID).toString());
		publicHeader.setIsIndividual(false);
		children.add(publicHeader);
		
		ughrp.setChildren(children);
		return ughrp;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createAclTest() throws Exception {		
		// create response ACL
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_inheritedACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).createAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// create
		acle.asWidget();
		acle.createAcl();
		//for one test case, also test for a successful callback
		acle.pushChangesToSynapse(false, new AsyncCallback<AccessControlList>() {
				@Override
				public void onSuccess(AccessControlList result) {
					assertEquals(localACL, result);
				}
				@Override
				public void onFailure(Throwable caught) {
					Assert.fail();
				}
			});
		
		
		verify(mockSynapseClient).createAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		assertEquals("Created ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(),anyBoolean());
		verify(mockACLEView).setPublicPrincipalIds(any(PublicPrincipalIds.class));
		
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addAccessTest() throws Exception {
		// create response ACL: add permissions for USER2
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(USER2_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		localACL.getResourceAccess().add(ra);
		ra = new ResourceAccess();
		ra.setPrincipalId(TEAM_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		localACL.getResourceAccess().add(ra);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.setAccess(TEAM_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));
		
		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		
		//add/remove public ready, verify it's reflected in UEP
		boolean canPublicRead = acle.getUserEntityPermissions().getCanPublicRead();
		assertFalse(canPublicRead);
		acle.setAccess(TEST_PUBLIC_PRINCIPAL_ID, PermissionLevel.CAN_VIEW);
		canPublicRead = acle.getUserEntityPermissions().getCanPublicRead();
		assertTrue("setting access to the public principal didn't update the user entity permissions (ACL editor view might be wrong)", canPublicRead);
		acle.removeAccess(TEST_PUBLIC_PRINCIPAL_ID);
		canPublicRead = acle.getUserEntityPermissions().getCanPublicRead();
		assertFalse("removing access to the public principal didn't update the user entity permissions (ACL editor view might be wrong)", canPublicRead);
		
		assertEquals("Updated ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(6)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
		verify(mockACLEView).setPublicPrincipalIds(any(PublicPrincipalIds.class));
		
		ArgumentCaptor<Set> recipientSetCaptor = ArgumentCaptor.forClass(Set.class);
		verify(mockSynapseClient).sendMessage(recipientSetCaptor.capture(), anyString(), anyString(), any(AsyncCallback.class));
		Set recipientSet = recipientSetCaptor.getValue();
		//should try to send a notification message to a single recipient principal id, USER2_ID.  Verify team is not notified
		assertEquals(1, recipientSet.size());
		assertTrue(recipientSet.contains(Long.toString(USER2_ID)));

	}
	
	@Test
	public void isNotifyFalseTest() throws Exception {
		when(mockACLEView.isNotifyPeople()).thenReturn(false);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		
		// update
		acle.asWidget();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addPublicAccessTest() throws Exception {
		// add view to public
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(TEST_PUBLIC_PRINCIPAL_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		localACL.getResourceAccess().add(ra);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		
		// update
		acle.asWidget();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));

		//verify we do not even attempt to send a message to public
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void changeAccessTest() throws Exception {
		// create response ACL: decrease ADMIN's permissions
		for (ResourceAccess resourceAccess : localACL.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				resourceAccess.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(),  any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));
		
		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		
		Set<ResourceAccess> localRAs = localACL.getResourceAccess();
		Set<ResourceAccess> returnedRAs = returnedACL.getResourceAccess();
		assertEquals(returnedRAs, localRAs);
		
		// commutativity bug in Java HashSet.equals()...?
		// assertEquals(localRAs, returnedRAs);
		
		localACL.setResourceAccess(null);
		returnedACL.setResourceAccess(null);
		assertTrue(localACL.equals(returnedACL));
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
		
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessTest() throws Exception {
		// create response ACL: remove ADMIN's permissions
		ResourceAccess toRemove = null;
		for (ResourceAccess resourceAccess : localACL.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				toRemove = resourceAccess;
		localACL.getResourceAccess().remove(toRemove);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.removeAccess(USER_ID);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));
		
		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		
		assertEquals("Updated ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
		
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deleteAclTest() throws Exception {
		// create response ACL: benefactor's
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).deleteAcl(eq(ENTITY_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).getNodeAcl(anyString(), any(AsyncCallback.class));
		
		// update
		acle.asWidget();
		acle.deleteAcl();
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));
		
		verify(mockSynapseClient).deleteAcl(eq(ENTITY_ID), any(AsyncCallback.class));
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
		
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateAclRecursiveTest() throws Exception {
		// create response ACL: add permissions for USER
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(USER_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		localACL.getResourceAccess().add(ra);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(true,mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).onSuccess(any(AccessControlList.class));
		
		verify(mockSynapseClient).updateAcl(captor.capture(), eq(true), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		
		assertEquals("Updated ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setAdminAccessTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		// update
		acle.asWidget();
		acle.setAccess(ADMIN_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);
		
		verify(mockACLEView).showErrorMessage(anyString());
		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void pushNoChangesTest() throws Exception {		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		// attempt to push changes when none have been made
		acle.asWidget();
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);
		
		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessNotFoundTest() throws Exception {		
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		// attempt to remove permissions for user not on ACL
		acle.asWidget();
		acle.removeAccess(USER2_ID);
		acle.pushChangesToSynapse(false,mockPushToSynapseCallback);

		verify(mockACLEView).showErrorMessage(anyString());
		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
	}

	@Test
	public void testUnsavedViewChanges() {
		acle.setUnsavedViewChanges(true);
		acle.pushChangesToSynapse(false, null);
		
		verify(mockACLEView).alertUnsavedViewChanges(any(Callback.class));
	}
	
}
