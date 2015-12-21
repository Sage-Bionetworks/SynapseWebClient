package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.util.ModelConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.FileValidator;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamEditModalWidgetTest {
	public static final Long AUTHENTICATED_USERS_GROUP_ID = 938L;
	public static final Long TEAM_ID = 1114L;
	TeamEditModalWidget presenter;
	SynapseAlert mockSynAlert;
	TeamEditModalWidgetView mockView;
	SynapseJSNIUtils mockJSNIUtils;
	SynapseClientAsync mockSynapseClient;
	FileHandleUploadWidget mockUploader;
	AuthenticationController mockAuthenticationController;
	Team mockTeam;
	Callback mockRefreshCallback;
	FileUpload mockFileUpload;
	FileMetadata mockFileMeta;
	
	Callback startedUploadingCallback;
	CallbackP<FileUpload> finishedUploadingCallback;
	String oldName = "oldName";
	String newName = "newName";
	String oldDesc = "oldDesc";
	String newDesc = "newDesc";
	boolean oldPublicJoin = false;
	boolean newPublicJoin = true;
	String oldIcon = "oldIcon";
	String newIcon = "newIcon";
	Exception caught = new Exception("this is an exception");
	private AccessControlList acl;
	
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockSynAlert = mock(SynapseAlert.class);
		mockView = mock(TeamEditModalWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockUploader = mock(FileHandleUploadWidget.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockTeam = mock(Team.class);
		when(mockTeam.getId()).thenReturn(TEAM_ID.toString());
		acl = createACL(TEAM_ID);
		mockRefreshCallback = mock(Callback.class);
		mockFileUpload = mock(FileUpload.class);
		mockFileMeta = mock(FileMetadata.class);
		when(mockGlobalApplicationState.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID)).thenReturn(AUTHENTICATED_USERS_GROUP_ID.toString());
		presenter = new TeamEditModalWidget(mockSynAlert, mockView, mockSynapseClient,
				mockUploader, mockJSNIUtils, mockAuthenticationController, mockGlobalApplicationState);
		AsyncMockStubber.callSuccessWith(acl).when(mockSynapseClient).getTeamAcl(eq(TEAM_ID.toString()), any(AsyncCallback.class));
		when(mockTeam.getIcon()).thenReturn(oldIcon);
		presenter.setRefreshCallback(mockRefreshCallback);
		
		ArgumentCaptor<Callback> startedUploadingCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockUploader).setUploadingCallback(startedUploadingCaptor.capture());
		// can invoke to check when loading finishes
		startedUploadingCallback = startedUploadingCaptor.getValue();
		
		ArgumentCaptor<CallbackP> finishedUploadingCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockUploader).configure(anyString(), finishedUploadingCaptor.capture());
		// can invoke to check when loading finishes
		finishedUploadingCallback = finishedUploadingCaptor.getValue();
		
		AsyncMockStubber.callSuccessWith(mockTeam).when(mockSynapseClient)
				.updateTeam(eq(mockTeam), eq(acl), any(AsyncCallback.class));
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMeta);
		when(mockFileMeta.getFileName()).thenReturn("new filename");
		when(mockView.getName()).thenReturn(newName);
		when(mockView.getDescription()).thenReturn(newDesc);
		when(mockView.getPublicJoin()).thenReturn(newPublicJoin);
	}
	
	public static AccessControlList createACL(Long teamId) {
		// create the set of permissions
		Set<ResourceAccess> resourceAccesses = new HashSet<ResourceAccess>();
		
		// create the ACL
		AccessControlList acl = new AccessControlList();
		acl.setId(teamId.toString());
		acl.setResourceAccess(resourceAccesses);
		return acl;
	}
	
	public static void addCanMessageTeam(Long principalId, AccessControlList acl) {
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(principalId);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_MESSAGE_TEAM));
		
		acl.getResourceAccess().add(ra);
	}
	@Test
	public void testConstruction() {
		verify(mockJSNIUtils).getBaseFileHandleUrl();
		verify(mockUploader).configure(anyString(), any(CallbackP.class));
		verify(mockUploader).setUploadingCallback(any(Callback.class));
		verify(mockView).setUploadWidget(mockUploader.asWidget());
		verify(mockView).setAlertWidget(mockSynAlert.asWidget());
		verify(mockView).setPresenter(presenter);
		
		//also verify that max image size is set
		ArgumentCaptor<FileValidator> captor = ArgumentCaptor.forClass(FileValidator.class);
		verify(mockUploader).setValidation(captor.capture());
		FileValidator validator = captor.getValue();
		assertTrue(validator instanceof ImageFileValidator);
		ImageFileValidator v = (ImageFileValidator)validator;
		assertEquals(UserProfileEditorWidgetImpl.MAX_IMAGE_SIZE, v.getMaxFileSize(), .1);
	}
	
	@Test
	public void testConfirmNoName() {
		when(mockView.getName()).thenReturn("");
		presenter.onConfirm();
		verify(mockSynAlert).showError("You must provide a name.");
	}
	
	@Test
	public void testImageLoading() {
	}
	
	@Test
	public void testConfirmSuccessfulChanges() {
		presenter.configureAndShow(mockTeam);
		when(mockFileUpload.getFileHandleId()).thenReturn("newIcon");
		
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView, times(2)).hideLoading();
		verify(mockView, times(2)).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockView).getName();
		verify(mockView).getDescription();
		verify(mockView).getPublicJoin();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), eq(acl), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}
	@Test
	public void testErrorOnTeamACLLoad() {
		String error = "error on team acl load";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getTeamAcl(eq(TEAM_ID.toString()), any(AsyncCallback.class));
		presenter.configureAndShow(mockTeam);
		verify(mockSynAlert).showError(error);
	}
	
	@Test
	public void testConfirmNoIconUploaded() {
		presenter.configureAndShow(mockTeam);
		when(mockFileUpload.getFileHandleId()).thenReturn(null);
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView, times(2)).hideLoading();
		verify(mockView, times(2)).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockView).getName();
		verify(mockView).getDescription();
		verify(mockView).getPublicJoin();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam, never()).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), eq(acl), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}	
	
	@Test
	public void testConfirmFailedChanges() {
		presenter.configureAndShow(mockTeam);
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient)
				.updateTeam(eq(mockTeam), eq(acl), any(AsyncCallback.class));
		when(mockFileUpload.getFileHandleId()).thenReturn(newIcon);
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView, times(2)).hideLoading();
		verify(mockView, times(2)).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), eq(acl), any(AsyncCallback.class));
		verify(mockUploader, times(2)).reset();
		verify(mockView, Mockito.atLeast(1)).hideLoading();
		verify(mockSynAlert).handleException(caught);
		verify(mockView, never()).hide();
	}
	
	@Test
	public void testShowIconExists() {
		presenter.configureAndShow(mockTeam);
		verify(mockUploader).reset();
		verify(mockSynAlert).clear();
		verify(mockView).hideLoading();
		verify(mockView).configure(mockTeam);
		verify(mockView).setImageURL(anyString());
		verify(mockView).show();
	}
	
	@Test
	public void testShowIconDoesNotExist() {
		when(mockTeam.getIcon()).thenReturn(null);
		presenter.configureAndShow(mockTeam);
		verify(mockUploader).reset();
		verify(mockSynAlert).clear();
		verify(mockView).hideLoading();
		verify(mockView).configure(mockTeam);
		verify(mockView).setDefaultIconVisible();
		verify(mockView).show();
	}
	
	@Test
	public void testHide() {
		presenter.hide();
		verify(mockView).hide();
		verify(mockView, never()).setDefaultIconVisible();
		verify(mockView, never()).setImageURL(anyString());
	}
	
	@Test
	public void testUpdateACLFromViewAuthenticatedUsersCanSend() {
		presenter.configureAndShow(mockTeam);
		//set up team can message, should not be touched in the acl.  Authenticated users should be added.
		addCanMessageTeam(TEAM_ID, acl);
		when(mockView.canAuthenticatedUsersSendMessageToTeam()).thenReturn(true);
		presenter.updateACLFromView();
		
		//verify that stubbed acl has been modified in the way that we expect
		assertEquals(2, acl.getResourceAccess().size());
		ResourceAccess teamRA = null, authenticatedUsersGroupRA = null;
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (AUTHENTICATED_USERS_GROUP_ID.equals(ra.getPrincipalId())) {
				authenticatedUsersGroupRA = ra;
			} else if (TEAM_ID.equals(ra.getPrincipalId())) {
				teamRA = ra;
			}
		}
		assertTrue(teamRA != null && authenticatedUsersGroupRA != null);
		assertEquals(ModelConstants.TEAM_MESSENGER_PERMISSIONS, authenticatedUsersGroupRA.getAccessType());
	}
	
	@Test
	public void testUpdateACLFromViewTeamMembersCanSend() {
		presenter.configureAndShow(mockTeam);
		//set up authenticated users can message, should be removed from acl.
		addCanMessageTeam(AUTHENTICATED_USERS_GROUP_ID, acl);
		addCanMessageTeam(TEAM_ID, acl);
		when(mockView.canAuthenticatedUsersSendMessageToTeam()).thenReturn(false);
		presenter.updateACLFromView();
		
		//verify that stubbed acl has been modified in the way that we expect
		assertEquals(1, acl.getResourceAccess().size());
		ResourceAccess ra = acl.getResourceAccess().iterator().next();
		assertEquals(TEAM_ID, ra.getPrincipalId());
		assertEquals(ModelConstants.TEAM_MESSENGER_PERMISSIONS, ra.getAccessType());
	}
	
}