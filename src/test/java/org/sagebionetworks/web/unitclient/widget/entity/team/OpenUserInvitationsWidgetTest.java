package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetView;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenUserInvitationsWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	OpenUserInvitationsWidgetView mockView;
	String teamId = "123";
	OpenUserInvitationsWidget widget;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	Callback mockTeamUpdatedCallback;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	UserProfile testProfile;
	MembershipInvitation testInvite;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(OpenUserInvitationsWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		widget = new OpenUserInvitationsWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockNodeModelCreator);
		
		
		testProfile = new UserProfile();
		testProfile.setOwnerId("42");
		testProfile.setFirstName("Bob");
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(UserProfile.class))).thenReturn(testProfile);
		testInvite = new MembershipInvitation();
		testInvite.setTeamId(teamId);
		testInvite.setUserId(testProfile.getOwnerId());
		testInvite.setMessage("This is a test invite");
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(MembershipInvitation.class))).thenReturn(testInvite);
		
		List<MembershipInvitationBundle> testReturn = new ArrayList<MembershipInvitationBundle>();
		testReturn.add(new MembershipInvitationBundle());
		
		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenTeamInvitations(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() throws Exception {
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).configure(anyList(), anyList());
	}
	
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getOpenTeamInvitations(anyString(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteOpenInvite() throws Exception {
		widget.configure(teamId, mockTeamUpdatedCallback);
		widget.removeInvitation("123");
		verify(mockSynapseClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
		verify(mockTeamUpdatedCallback).invoke();
		verify(mockView).configure(anyList(), anyList());
	}
	
	public void testDeleteOpenInviteFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		widget.removeInvitation("123");
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	
}
