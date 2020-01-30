package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ErrorResponseCode;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlListEditorTest {
	private static final String HOST_PAGE_BASE_URL = "http://www.wwu.edu/";
	// The ACLEditor
	private AccessControlListEditor acle;

	// Mock components
	@Mock
	private SynapseClientAsync mockSynapseClient;
	@Mock
	private GWTWrapper mockGwt;
	private AuthenticationController mockAuthenticationController;
	@Mock
	private AccessControlListEditorView mockACLEView;
	@Mock
	private HasChangesHandler mockHasChangeHandler;
	@Mock
	private Callback mockPushToSynapseCallback;

	// Test Synapse objects
	private static final long OWNER_ID = 1L;
	private static final long ADMIN_ID = 2L;
	private static final long USER_ID = 3L;
	private static final long USER2_ID = 4L;
	private static final long TEAM_ID = 5L;
	private static final Long TEST_PUBLIC_PRINCIPAL_ID = 789l;
	private static final String OWNER_NAME = "Owner";
	private static final String ENTITY_ID = "syn101";
	private static final String INHERITED_ACL_ID = "syn202";
	private static AccessControlList localACL;
	private static AccessControlList inheritedACL;
	private static EntityBundle entityBundleTransport_localACL;
	private static EntityBundle entityBundleTransport_inheritedACL;
	private static Project project;
	private static UserGroupHeaderResponsePage userGroupHeaderRP;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Captor
	ArgumentCaptor<ArrayList<String>> listCaptor;
	@Mock
	PublicPrincipalIds mockPublicPrincipalIds;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	QuizInfoDialog mockQuizInfoDialog;
	@Mock
	PortalGinInjector mockPortalGinInjector;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JSONObjectAdapterException {
		// set up test Synapse objects
		project = createProject();
		localACL = createACL(ENTITY_ID);
		AccessControlList localAclClone = createACL(ENTITY_ID);
		inheritedACL = createACL(INHERITED_ACL_ID);
		AccessControlList inheritedAclClone = createACL(INHERITED_ACL_ID);
		entityBundleTransport_localACL = createEBT(localAclClone, createUEP());
		entityBundleTransport_inheritedACL = createEBT(inheritedAclClone, createUEP());
		userGroupHeaderRP = createUGHRP();

		// set up mocks
		mockAuthenticationController = mock(AuthenticationController.class, RETURNS_DEEP_STUBS);
		when(mockSynapseProperties.getPublicPrincipalIds()).thenReturn(mockPublicPrincipalIds);
		when(mockPublicPrincipalIds.getPublicAclPrincipalId()).thenReturn(TEST_PUBLIC_PRINCIPAL_ID);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(new Long(ADMIN_ID).toString());
		AsyncMockStubber.callSuccessWith(userGroupHeaderRP).when(mockSynapseJavascriptClient).getUserGroupHeadersById(Matchers.<ArrayList<String>>any(), any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));

		// instantiate the ACLEditor
		acle = new AccessControlListEditor(mockACLEView, mockSynapseClient, mockAuthenticationController, mockSynapseProperties, mockGwt, mockSynapseJavascriptClient, mockSynAlert, mockPortalGinInjector);
		acle.configure(project, true, mockHasChangeHandler);
		when(mockACLEView.isNotifyPeople()).thenReturn(true);
		when(mockGwt.getHostPageBaseURL()).thenReturn(HOST_PAGE_BASE_URL);
		when(mockPortalGinInjector.getQuizInfoDialog()).thenReturn(mockQuizInfoDialog);
	}

	private static Project createProject() {
		Project p = new Project();
		p.setId(ENTITY_ID);
		p.setCreatedBy(OWNER_NAME);
		return p;
	}

	private static EntityBundle createEBT(AccessControlList acl, UserEntityPermissions uep) {
		try {
			EntityBundle ebt = new EntityBundle();
			ebt.setBenefactorAcl(acl);
			ebt.setPermissions(uep);
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

	@Test
	public void testProjectPermissionLevels() {
		assertEquals(AccessControlListEditor.PERMISSIONS, acle.getPermList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createAclTest() throws Exception {
		// create response ACL
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_inheritedACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).createAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// create
		acle.refresh();
		acle.createAcl();
		// for one test case, also test for a successful callback
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).createAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());
		assertEquals("Created ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		// verify initially inherited acl, then local ACL
		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), eq(INHERITED_ACL_ID), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());
		verify(mockACLEView, times(2)).buildWindow(anyBoolean(), anyBoolean(), eq(ENTITY_ID), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());

		verify(mockACLEView).setPublicAclPrincipalId(any(Long.class));

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.refresh();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.setAccess(TEAM_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());

		// add/remove public ready, verify it's reflected in UEP
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
		verify(mockACLEView, times(6)).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());
		verify(mockACLEView).setPublicAclPrincipalId(any(Long.class));

		ArgumentCaptor<Set> recipientSetCaptor = ArgumentCaptor.forClass(Set.class);
		verify(mockSynapseClient).sendMessage(recipientSetCaptor.capture(), anyString(), anyString(), eq(HOST_PAGE_BASE_URL), any(AsyncCallback.class));
		Set recipientSet = recipientSetCaptor.getValue();
		// should try to send a notification message to a single recipient principal id, USER2_ID. Verify
		// team is not notified
		assertEquals(1, recipientSet.size());
		assertTrue(recipientSet.contains(Long.toString(USER2_ID)));

	}

	@Test
	public void isNotifyFalseTest() throws Exception {
		when(mockACLEView.isNotifyPeople()).thenReturn(false);

		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));

		// update
		acle.refresh();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));

		// update
		acle.refresh();

		// SWC-3602: verify that it asked for the public user group headers
		verify(mockSynapseJavascriptClient).getUserGroupHeadersById(listCaptor.capture(), any(AsyncCallback.class));
		ArrayList<String> ids = listCaptor.getValue();
		assertTrue(ids.contains(TEST_PUBLIC_PRINCIPAL_ID.toString()));

		acle.setAccess(USER2_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		// verify we do not even attempt to send a message to public
		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void changeAccessTest() throws Exception {
		// create response ACL: decrease ADMIN's permissions
		for (ResourceAccess resourceAccess : localACL.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				resourceAccess.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));

		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.refresh();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

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
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.refresh();
		acle.removeAccess(USER_ID);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());

		assertEquals("Updated ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAclTest() throws Exception {
		// create response ACL: benefactor's

		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).deleteAcl(eq(ENTITY_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).getEntityBenefactorAcl(anyString(), any(AsyncCallback.class));

		// update
		acle.refresh();
		acle.deleteAcl();
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).deleteAcl(eq(ENTITY_ID), any(AsyncCallback.class));
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());

		verify(mockSynapseClient, never()).sendMessage(anySet(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void recreateAclTest() throws Exception {
		// SWC-3795 test.
		// create response ACL: benefactor's
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).deleteAcl(eq(ENTITY_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(inheritedACL).when(mockSynapseClient).getEntityBenefactorAcl(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		String etag = "12843";
		localACL.setEtag(etag);
		entityBundleTransport_localACL.setBenefactorAcl(localACL);

		// update
		acle.refresh();
		acle.deleteAcl();
		acle.createAcl();
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).updateAcl(captor.capture(), eq(false), any(AsyncCallback.class));
		AccessControlList updateAcl = captor.getValue();
		assertEquals(localACL.getEtag(), updateAcl.getEtag());
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
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(localACL).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);

		// update
		acle.refresh();
		acle.setAccess(USER_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(true, mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();

		verify(mockSynapseClient).updateAcl(captor.capture(), eq(true), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		localACL.setCreationDate(returnedACL.getCreationDate());

		assertEquals("Updated ACL is invalid", localACL, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
		verify(mockACLEView, times(3)).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setAdminAccessTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		boolean isLoggedIn = true;
		when(mockAuthenticationController.isLoggedIn()).thenReturn(isLoggedIn);
		// update
		acle.refresh();
		acle.setAccess(ADMIN_ID, PermissionLevel.CAN_VIEW);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);

		boolean isProject = true;
		boolean isInherited = false;
		String aclEntityId = ENTITY_ID;
		boolean canEnableInheritance = true;
		boolean canChangePermission = true;
		verify(mockACLEView, times(2)).buildWindow(eq(isProject), eq(isInherited), eq(aclEntityId), eq(canEnableInheritance), eq(canChangePermission), eq(PermissionLevel.CAN_DOWNLOAD), eq(isLoggedIn));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void pushNoChangesTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));

		// attempt to push changes when none have been made
		acle.refresh();
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);

		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());
	}

	@Test
	public void addDownloadAccessToAuthenticatedUsersNotCertified() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(new ForbiddenException("Only certified users can allow authenticated users to download.", ErrorResponseCode.USER_CERTIFICATION_REQUIRED)).when(mockSynapseClient).updateAcl(any(AccessControlList.class), anyBoolean(), any(AsyncCallback.class));

		acle.refresh();
		acle.setAccess(TEST_PUBLIC_PRINCIPAL_ID, PermissionLevel.CAN_DOWNLOAD);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);

		verify(mockQuizInfoDialog).show();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessNotFoundTest() throws Exception {
		// configure mocks
		AsyncMockStubber.callSuccessWith(entityBundleTransport_localACL).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));

		// attempt to remove permissions for user not on ACL
		acle.refresh();
		acle.removeAccess(USER2_ID);
		acle.pushChangesToSynapse(false, mockPushToSynapseCallback);

		verify(mockSynAlert).showError(anyString());
		verify(mockACLEView).buildWindow(anyBoolean(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), eq(PermissionLevel.CAN_DOWNLOAD), anyBoolean());
	}
}
