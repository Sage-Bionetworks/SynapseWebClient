package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MemberListWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	MemberListWidgetView mockView;
	String teamId = "123";
	MemberListWidget widget;
	AuthenticationController mockAuthenticationController;
	Callback mockTeamUpdatedCallback;
	boolean isAdmin;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	SynapseAlert mockSynAlert;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(MemberListWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		widget = new MemberListWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState, mockGWT, mockSynAlert);
		isAdmin = true;
		
		AsyncMockStubber.callSuccessWith(getTestTeamMembers()).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}
	
	private TeamMemberPagedResults getTestTeamMembers() {
		TeamMemberPagedResults teamMembers = new TeamMemberPagedResults();
		
		List<TeamMemberBundle> teamMemberList = new ArrayList<TeamMemberBundle>();
		TeamMemberBundle team = new TeamMemberBundle();
		team.setIsAdmin(true);
		team.setUserProfile(new UserProfile());
		team.setTeamId(teamId);
		teamMemberList.add(team);
		
		team = new TeamMemberBundle();
		team.setIsAdmin(false);
		team.setUserProfile(new UserProfile());
		team.setTeamId(teamId);
		teamMemberList.add(team);
		
		teamMembers.setResults(teamMemberList);
		teamMembers.setTotalNumberOfResults((long)teamMemberList.size());
		return teamMembers;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() throws Exception {
		//verify it tries to refresh all members
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addMembers(anyList(), anyBoolean());
	}
	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveMember() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		widget.removeMember("a user id");
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}
	@Test
	public void testRemoveMemberFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.removeMember("a user id");
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	@Test
	public void testRemoveMemberBadRequestFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		String badRequestMessage = "Team must have at least one administrator.";
		Exception ex = new BadRequestException(badRequestMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		widget.removeMember("a user id");
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsAdmin() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		widget.setIsAdmin("a user id", true);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}
	
	@Test
	public void testSetIsAdminFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		widget.setIsAdmin("a user id", true);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		//called twice.  once during configuration, and once to refresh members to get correct admin state
		verify(mockSynapseClient, times(2)).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testCheckForInViewAndLoadDataNotAttached() {
		when(mockView.isLoadMoreAttached()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT, never()).scheduleExecution(any(Callback.class), anyInt());
		verify(mockSynapseClient, never()).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedNotInViewport() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockSynapseClient, never()).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedNotVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(true);
		when(mockView.getLoadMoreVisibility()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockSynapseClient, never()).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(false);
		when(mockView.getLoadMoreVisibility()).thenReturn(true);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockSynapseClient, never()).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedInViewAndVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(true);
		when(mockView.getLoadMoreVisibility()).thenReturn(true);
		widget.checkForInViewAndLoadData();
		verify(mockGWT, never()).scheduleExecution(any(Callback.class), anyInt());
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
}
