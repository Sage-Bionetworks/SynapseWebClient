package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class MemberListWidgetTest {

	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	MemberListWidgetView mockView;
	String teamId = "123";
	MemberListWidget widget;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	Callback mockTeamUpdatedCallback;
	boolean isAdmin;
	@Mock
	LoadMoreWidgetContainer mockMembersContainer;
	@Mock
	SynapseJavascriptClient mockJsClient;

	@Before
	public void before() throws JSONObjectAdapterException {
		widget = new MemberListWidget(mockView, mockSynapseClient, mockJsClient, mockAuthenticationController, mockGlobalApplicationState, mockMembersContainer);
		isAdmin = true;

		AsyncMockStubber.callSuccessWith(getTestTeamMembers()).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
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
		teamMembers.setTotalNumberOfResults((long) teamMemberList.size());
		return teamMembers;
	}

	@Test
	public void testConfigure() {
		// verify it tries to refresh all members
		TeamMemberTypeFilterOptions memberType = TeamMemberTypeFilterOptions.ALL;
		widget.configure(teamId, isAdmin, memberType, mockTeamUpdatedCallback);
		verify(mockJsClient).getTeamMembers(anyString(), anyString(), eq(memberType), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addMembers(anyList(), anyBoolean());
		verify(mockMembersContainer).configure(any(Callback.class));
	}

	@Test
	public void testConfigureFailure() {
		TeamMemberTypeFilterOptions memberType = TeamMemberTypeFilterOptions.MEMBER;
		String error = "unhandled exception";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(teamId, isAdmin, memberType, mockTeamUpdatedCallback);
		verify(mockJsClient).getTeamMembers(anyString(), anyString(), eq(memberType), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(error);
	}

	@Test
	public void testRemoveMember() {
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		widget.removeMember("a user id");
		verify(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}

	@Test
	public void testRemoveMemberFailure() {
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		String error = "unhandled exception";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
		widget.removeMember("a user id");
		verify(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(error);
	}

	@Test
	public void testRemoveMemberBadRequestFailure() {
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		String badRequestMessage = "Team must have at least one administrator.";
		Exception ex = new BadRequestException(badRequestMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
		widget.removeMember("a user id");
		verify(mockJsClient).deleteTeamMember(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(badRequestMessage);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsAdmin() {
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		widget.setIsAdmin("a user id", true);
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}

	@Test
	public void testSetIsAdminFailure() {
		String message = "unhandled exception";
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		Exception ex = new Exception(message);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		widget.setIsAdmin("a user id", true);
		verify(mockSynapseClient).setIsTeamAdmin(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(message);;
		// called twice. once during configuration, and once to refresh members to get correct admin state
		verify(mockJsClient, times(2)).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testSearch() {
		widget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ALL, mockTeamUpdatedCallback);
		String searchTerm = "Xa";
		Long totalNumberOfResults = 10L;
		TeamMemberPagedResults searchResults = new TeamMemberPagedResults();
		searchResults.setResults(new ArrayList<>());
		searchResults.setTotalNumberOfResults(totalNumberOfResults);
		AsyncMockStubber.callSuccessWith(searchResults).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));

		widget.search(searchTerm);

		verify(mockJsClient).getTeamMembers(anyString(), eq(searchTerm), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
	}
}
