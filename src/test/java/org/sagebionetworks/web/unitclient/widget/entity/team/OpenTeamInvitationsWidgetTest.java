package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidgetView;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetView;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenTeamInvitationsWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	OpenTeamInvitationsWidgetView mockView;
	String teamId = "123";
	OpenTeamInvitationsWidget widget;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	Team testTeam;
	MembershipInvitation testInvite;
	List<MembershipInvitationBundle> testReturn;
	Callback mockTeamUpdatedCallback;
	CallbackP<List<MembershipInvitationBundle>> mockOpenTeamInvitationsCallback;
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(OpenTeamInvitationsWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		widget = new OpenTeamInvitationsWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockNodeModelCreator);
		
		testTeam = new Team();
		testTeam.setId(teamId);
		testTeam.setName("Bob's Team");
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(Team.class))).thenReturn(testTeam);
		testInvite = new MembershipInvitation();
		testInvite.setTeamId(teamId);
		testInvite.setUserId("42");
		testInvite.setMessage("This is a test invite");
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(MembershipInvitation.class))).thenReturn(testInvite);
		
		testReturn = new ArrayList<MembershipInvitationBundle>();
		testReturn.add(new MembershipInvitationBundle());
		
		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		
		mockOpenTeamInvitationsCallback = mock(CallbackP.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() throws Exception {
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		verify(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).configure(anyListOf(Team.class), anyListOf(String.class));
		ArgumentCaptor<List> invitesArg = ArgumentCaptor.forClass(List.class);				   
		verify(mockOpenTeamInvitationsCallback).invoke(invitesArg.capture());
		assertEquals(testReturn, invitesArg.getValue());
	}
	
	public void testConfigureFailureGetOpenInvites() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		verify(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testJoin() throws Exception {
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		widget.joinTeam(teamId);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}
	
	@Test
	public void testJoinFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		widget.joinTeam(teamId);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		verify(mockTeamUpdatedCallback, never()).invoke();
	}
	
}
