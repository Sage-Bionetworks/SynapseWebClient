package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetView;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenUserInvitationsWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	OpenUserInvitationsWidgetView mockView;
	String teamId = "123";
	OpenUserInvitationsWidget widget;
	AuthenticationController mockAuthenticationController;
	Callback mockTeamUpdatedCallback;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	UserProfile testProfile;
	MembershipInvtnSubmission testInvite;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(OpenUserInvitationsWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		widget = new OpenUserInvitationsWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController);
		
		
		testProfile = new UserProfile();
		testProfile.setOwnerId("42");
		testProfile.setFirstName("Bob");
		testInvite = new MembershipInvtnSubmission();
		testInvite.setTeamId(teamId);
//		testInvite.setUserId(testProfile.getOwnerId());
		testInvite.setMessage("This is a test invite");
		
		List<OpenTeamInvitationBundle> testReturn = new ArrayList<OpenTeamInvitationBundle>();
		OpenTeamInvitationBundle mib = new OpenTeamInvitationBundle();
		mib.setUserProfile(testProfile);
		mib.setMembershipInvtnSubmission(testInvite);
		testReturn.add(mib);
		
		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(),anyInt(),any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
	}
	
	private void setupGetOpenTeamInvitations(int mockInvitationReturnCount) {
		List<OpenTeamInvitationBundle> testReturn = new ArrayList<OpenTeamInvitationBundle>();
		for (int i = 0; i < mockInvitationReturnCount; i++) {
			testReturn.add(new OpenTeamInvitationBundle());	
		}
		
		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(),anyInt(),any(AsyncCallback.class));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() throws Exception {
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).configure(anyList(), anyList());
	}
	@Test
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(),anyInt(),any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteOpenInvite() throws Exception {
		String invitationId = "123";
		widget.configure(teamId, mockTeamUpdatedCallback);
		widget.removeInvitation(invitationId);
		verify(mockSynapseClient).deleteMembershipInvitation(eq(invitationId), any(AsyncCallback.class));
		verify(mockTeamUpdatedCallback).invoke();
		verify(mockView).configure(anyList(), anyList());
	}
	
	@Test
	public void testDeleteOpenInviteFailure() throws Exception {
		String invitationId = "123";
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		widget.removeInvitation(invitationId);
		verify(mockSynapseClient).deleteMembershipInvitation(eq(invitationId), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	@Test
	public void testMoreResultsUnavailable() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT - 1);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).setMoreResultsVisible(eq(false));
	}
	@Test
	public void testNoResultsAvailable() throws Exception {
		setupGetOpenTeamInvitations(0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).setMoreResultsVisible(eq(false));
	}
	@Test
	public void testMoreResultsAvailableGetNextBatch() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).setMoreResultsVisible(eq(true));
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(0), any(AsyncCallback.class));
		
		//simulate that there are really no more results
		reset(mockSynapseClient);
		setupGetOpenTeamInvitations(0);
		widget.getNextBatch();
		verify(mockView).setMoreResultsVisible(eq(false));
		//offset should now be 1*OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), any(AsyncCallback.class));
	}
	@Test
	public void testNoResultsFoundGetNextBatch() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).setMoreResultsVisible(eq(true));
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(0), any(AsyncCallback.class));
		
		//simulate that there are really no more results
		reset(mockSynapseClient);
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(),anyInt(),any(AsyncCallback.class));
		widget.getNextBatch();
		verify(mockView).setMoreResultsVisible(eq(false));
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), any(AsyncCallback.class));
	}
	
	
	
}
