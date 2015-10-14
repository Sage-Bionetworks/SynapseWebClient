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
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.TeamAccessControlListEditor;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamAccessControlListEditorTest {
	
	// The ACLEditor
	private TeamAccessControlListEditor acle;
	
	@Mock
	private SynapseClientAsync mockSynapseClient;
	@Mock
	private AuthenticationController mockAuthenticationController;
	@Mock
	private AccessControlListEditorView mockACLEView;
	@Mock
	private Callback mockPushToSynapseCallback;
	@Mock
	private GlobalApplicationState mockGlobalApplicationState;
	
	// Test Synapse objects
	
	private static final long ADMIN_ID = 2L;
	private static final long USER_ID = 3L;
	private static final long USER2_ID = 4L;
	private static final String TEAM_ID = "101";
	
	private static AccessControlList acl, aclClone;
	private static Team team;
	private static UserGroupHeaderResponsePage userGroupHeaderRP;
	AccessControlListEditor.HasChangesHandler mockHasChangesHandler;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		// set up test Synapse objects
		team = new Team();
		team.setId(TEAM_ID);
		team.setName("Test Team");
		acl = createACL(TEAM_ID);
		aclClone = createACL(TEAM_ID); 
		userGroupHeaderRP = AccessControlListEditorTest.createUGHRP();
		
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(new Long(ADMIN_ID).toString());
		AsyncMockStubber.callSuccessWith(userGroupHeaderRP).when(mockSynapseClient).getUserGroupHeadersById(Matchers.<ArrayList<String>>any(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(acl).when(mockSynapseClient).getTeamAcl(anyString(), any(AsyncCallback.class));
		mockPushToSynapseCallback = mock(Callback.class);
		
		// instantiate the ACLEditor
		acle = new TeamAccessControlListEditor(mockACLEView,
				mockSynapseClient,
				mockAuthenticationController,
				mockGlobalApplicationState
		);
		
		mockHasChangesHandler = mock(AccessControlListEditor.HasChangesHandler.class);
		acle.configure(team, mockHasChangesHandler);
		acle.refresh();
	}
	

	public static AccessControlList createACL(String teamId) {
		// create the set of permissions
		Set<ResourceAccess> resourceAccesses = new HashSet<ResourceAccess>();
		
		// add admin user
		ResourceAccess adminRA = new ResourceAccess();
		adminRA.setPrincipalId(ADMIN_ID);
		adminRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER_TEAM));
		resourceAccesses.add(adminRA);
		
		// add the non-owner non-admin user
		ResourceAccess userRA = new ResourceAccess();
		userRA.setPrincipalId(USER_ID);
		userRA.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_MESSAGE_TEAM));
		resourceAccesses.add(userRA);
		
		// create the ACL
		AccessControlList acl = new AccessControlList();
		acl.setId(teamId);
		acl.setResourceAccess(resourceAccesses);
		return acl;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addAccessTest() throws Exception {
		// create response ACL: add permissions for USER
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(USER2_ID);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_MESSAGE_TEAM));
		aclClone.getResourceAccess().add(ra);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(aclClone).when(mockSynapseClient).updateTeamAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.setAccess(USER2_ID, PermissionLevel.CAN_ADMINISTER_TEAM);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();
		
		verify(mockSynapseClient).updateTeamAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		acl.setCreationDate(returnedACL.getCreationDate());
		
		verify(mockACLEView, never()).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void changeAccessTest() throws Exception {
		// create response ACL: decrease ADMIN's permissions
		for (ResourceAccess resourceAccess : aclClone.getResourceAccess())
			if (resourceAccess.getPrincipalId().equals(USER_ID))
				resourceAccess.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_MESSAGE_TEAM));
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(aclClone).when(mockSynapseClient).updateTeamAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.setAccess(USER_ID, PermissionLevel.CAN_MESSAGE_TEAM);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();
		
		verify(mockSynapseClient).updateTeamAcl(captor.capture(), any(AsyncCallback.class));
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
			if (resourceAccess.getPrincipalId().equals(ADMIN_ID))
				toRemove = resourceAccess;
		aclClone.getResourceAccess().remove(toRemove);
		
		// configure mocks
		AsyncMockStubber.callSuccessWith(aclClone).when(mockSynapseClient).updateTeamAcl(any(AccessControlList.class), any(AsyncCallback.class));
		ArgumentCaptor<AccessControlList> captor = ArgumentCaptor.forClass(AccessControlList.class);
		
		// update
		acle.asWidget();
		acle.removeAccess(ADMIN_ID);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
		verify(mockPushToSynapseCallback).invoke();
		
		verify(mockSynapseClient).updateTeamAcl(captor.capture(), any(AsyncCallback.class));
		AccessControlList returnedACL = captor.getValue();
		acl.setCreationDate(returnedACL.getCreationDate());
		
		assertEquals("Updated ACL is invalid", acl, returnedACL);
		verify(mockACLEView, never()).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void pushNoChangesTest() throws Exception {		
		// configure mocks
		AsyncMockStubber.callSuccessWith(aclClone).when(mockSynapseClient).updateTeamAcl(any(AccessControlList.class), any(AsyncCallback.class));
		
		// attempt to push changes when none have been made
		acle.asWidget();
		acle.pushChangesToSynapse(mockPushToSynapseCallback);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeAccessNotFoundTest() throws Exception {		
		AsyncMockStubber.callSuccessWith(acl).when(mockSynapseClient).updateTeamAcl(any(AccessControlList.class), any(AsyncCallback.class));
		
		// attempt to remove permissions for user not on ACL
		acle.asWidget();
		acle.removeAccess(USER2_ID);
		acle.pushChangesToSynapse(mockPushToSynapseCallback);

		verify(mockACLEView).showErrorMessage(anyString());
	}
}
