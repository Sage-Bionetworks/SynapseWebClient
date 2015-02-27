package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
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
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	boolean isAdmin;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(MemberListWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		widget = new MemberListWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState, adapter);
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
		verify(mockView).configure(anyList(), anyString(), anyBoolean());
	}
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveMember() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		widget.removeMember("a user id");
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}

	public void testRemoveMemberFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.removeMember("a user id");
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	public void testRemoveMemberBadRequestFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		String badRequestMessage = "Team must have at least one administrator.";
		AsyncMockStubber.callFailureWith(new BadRequestException(badRequestMessage)).when(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.removeMember("a user id");
		verify(mockSynapseClient).deleteTeamMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(eq(badRequestMessage));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsAdmin() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		widget.setIsAdmin("a user id", true);
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}

	public void testSetIsAdminFailure() throws Exception {
		widget.configure(teamId, isAdmin, mockTeamUpdatedCallback);
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		widget.setIsAdmin("a user id", true);
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		//also refreshes members to get correct admin state
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
}
