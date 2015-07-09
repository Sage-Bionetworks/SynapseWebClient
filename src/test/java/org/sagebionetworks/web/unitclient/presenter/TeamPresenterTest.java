package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.TeamPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamPresenterTest {

	TeamPresenter presenter;
	TeamView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalAppState;
	SynapseClientAsync mockSynClient;
	JSONObjectAdapter mockJSONAdapter;
	SynapseAlert mockSynAlert;
	TeamLeaveModalWidget mockLeaveModal;
	TeamDeleteModalWidget mockDeleteModal;
	TeamEditModalWidget mockEditModal;
	InviteWidget mockInviteModal;
	JoinTeamWidget mockJoinWidget;
	MemberListWidget mockMemberListWidget;
	OpenMembershipRequestsWidget mockOpenMembershipRequestsWidget;
	OpenUserInvitationsWidget mockOpenUserInvitationsWidget;
	Team mockTeam;
	TeamBundle mockTeamBundle;
	TeamMembershipStatus mockTeamMembershipStatus;
	
	String teamId = "123";
	String teamName = "testTeam";
	boolean canPublicJoin = true;
	String teamIcon = "teamIcon";
	Long totalMembershipCount = 3L;
	Exception caught = new Exception("this is an exception");
	
	String newName = "newName";
	String newDesc = "newDesc";
	boolean newPublicJoin = false;
	String newIcon = "newIcon";
	
	@Before
	public void setup() {
		mockView = mock(TeamView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalAppState = mock(GlobalApplicationState.class);
		mockSynClient = mock(SynapseClientAsync.class);
		mockJSONAdapter = mock(JSONObjectAdapter.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockLeaveModal = mock(TeamLeaveModalWidget.class);
		mockDeleteModal = mock(TeamDeleteModalWidget.class);
		mockEditModal = mock(TeamEditModalWidget.class);
		mockInviteModal = mock(InviteWidget.class);
		mockJoinWidget = mock(JoinTeamWidget.class);
		mockMemberListWidget = mock(MemberListWidget.class);
		mockOpenMembershipRequestsWidget = mock(OpenMembershipRequestsWidget.class);
		mockOpenUserInvitationsWidget = mock(OpenUserInvitationsWidget.class);
		presenter = new TeamPresenter(mockView, mockAuthenticationController, mockGlobalAppState, mockSynClient, mockJSONAdapter, mockSynAlert, mockLeaveModal, mockDeleteModal, mockEditModal, mockInviteModal, mockJoinWidget, mockMemberListWidget, mockOpenMembershipRequestsWidget, mockOpenUserInvitationsWidget);
		mockTeam = mock(Team.class);
		when(mockTeam.getName()).thenReturn(teamName);
		mockTeamBundle = mock(TeamBundle.class);
		mockTeamMembershipStatus = mock(TeamMembershipStatus.class);
		AsyncMockStubber.callSuccessWith(mockTeamBundle).when(mockSynClient)
		.getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		//team bundle
		when(mockTeamBundle.getTeam()).thenReturn(mockTeam);
		when(mockTeamBundle.getTeamMembershipStatus()).thenReturn(mockTeamMembershipStatus);
		when(mockTeamBundle.getTotalMemberCount()).thenReturn(totalMembershipCount);
		
		//team
		when(mockTeam.getCanPublicJoin()).thenReturn(canPublicJoin);
		when(mockTeam.getId()).thenReturn(teamId);
		when(mockTeam.getIcon()).thenReturn(teamIcon);
	}
	
	@Test
	public void testRefreshFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynClient).getTeamBundle(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		presenter.refresh(teamId);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testRefreshAdmin() {
		boolean isAdmin = true;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(true);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(canPublicJoin);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockView).showMemberMenuItems();
		verify(mockOpenMembershipRequestsWidget).configure(eq(teamId), any(Callback.class));
		verify(mockOpenUserInvitationsWidget).configure(eq(teamId), any(Callback.class));
		verify(mockView).showAdminMenuItems();
		verify(mockView).setTeamEmailAddress(anyString());
		//never
		verify(mockJoinWidget, never()).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
	}
	
	@Test
	public void testRefreshNotMember() {
		boolean isAdmin = false;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(false);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(canPublicJoin);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockJoinWidget).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
	
		//never
		verify(mockView, never()).showMemberMenuItems();
		verify(mockOpenMembershipRequestsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockOpenUserInvitationsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockView, never()).showAdminMenuItems();
	}
	
	@Test
	public void testRefreshMember() {	
		boolean isAdmin = false;
		when(mockTeamBundle.isUserAdmin()).thenReturn(isAdmin);
		when(mockTeamMembershipStatus.getIsMember()).thenReturn(true);
		presenter.refresh(teamId);
		
		//once
		verify(mockView).setPublicJoinVisible(canPublicJoin);
		verify(mockView).setTotalMemberCount(totalMembershipCount.toString());
		verify(mockView).setMediaObjectPanel(mockTeam);
		verify(mockMemberListWidget).configure(eq(teamId), eq(isAdmin), any(Callback.class));
		verify(mockView).showMemberMenuItems();
		
		//never
		verify(mockJoinWidget, never()).configure(eq(teamId), anyBoolean(), eq(mockTeamMembershipStatus), 
				any(Callback.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
		verify(mockOpenMembershipRequestsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockOpenUserInvitationsWidget, never()).configure(eq(teamId), any(Callback.class));
		verify(mockView, never()).showAdminMenuItems();
	}

	@Test
	public void testGetTeamEmail() {
		assertEquals("basic@synapse.org", presenter.getTeamEmail("basic"));
		assertEquals("StandardCaseHere@synapse.org", presenter.getTeamEmail("Standard Case Here"));
		assertEquals("unlikelycase@synapse.org", presenter.getTeamEmail(" \n\r unlikely\t case "));
	}
	
}
