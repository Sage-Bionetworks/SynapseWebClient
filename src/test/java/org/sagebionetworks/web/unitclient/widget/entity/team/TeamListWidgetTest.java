package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamListWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	TeamListWidgetView mockView;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	String teamId = "123";
	TeamListWidget widget;
	AuthenticationController mockAuthenticationController;
	AsyncCallback<List<Team>> mockGetTeamsCallback;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	ArrayList<Team> teamList;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(TeamListWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGetTeamsCallback = mock(AsyncCallback.class);
		widget = new TeamListWidget(mockView, mockSynapseClient, mockGlobalApplicationState,mockAuthenticationController, adapter);
		teamList = setupUserTeams(adapter, mockSynapseClient);
	}
	
	public static ArrayList<Team> setupUserTeams(JSONObjectAdapter adapter, SynapseClientAsync mockSynapseClient) throws JSONObjectAdapterException {
		ArrayList<String> teamJsonList = new ArrayList<String>();
		Team testTeam = new Team();
		testTeam.setId("42");
		testTeam.setName("My Test Team");
		JSONObjectAdapter t = adapter.createNew();
		testTeam.writeToJSONObject(t);
		teamJsonList.add(t.toJSONString());
		ArrayList<Team> teamList = new ArrayList<Team>();
		teamList.add(testTeam);
		AsyncMockStubber.callSuccessWith(teamJsonList).when(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		return teamList;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTeams() throws Exception {
		widget.getTeams("12345",mockSynapseClient, adapterFactory, mockGetTeamsCallback);
		verify(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		verify(mockGetTeamsCallback).onSuccess(eq(teamList));
	}
	public void testGetTeamsFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		widget.getTeams("12345",mockSynapseClient, adapterFactory, mockGetTeamsCallback);
		verify(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		verify(mockGetTeamsCallback).onFailure(eq(ex));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetQueryForRequestCount() throws Exception {
		//when request count is null, should do nothing
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		widget.queryForRequestCount("12345");
		verify(mockView, times(0)).setRequestCount(anyString(), anyLong());
		verify(mockView, times(0)).showErrorMessage(anyString());

		//when request count is 0, should do nothing
		AsyncMockStubber.callSuccessWith(0l).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		widget.queryForRequestCount("12345");
		verify(mockView, times(0)).setRequestCount(anyString(), anyLong());
		verify(mockView, times(0)).showErrorMessage(anyString());

		//when request count is >0, should set the request count in the view
		AsyncMockStubber.callSuccessWith(1l).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		widget.queryForRequestCount("12345");
		verify(mockView).setRequestCount(anyString(), anyLong());
		verify(mockView, times(0)).showErrorMessage(anyString());
	}
	public void testGetQueryForRequestCountFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		widget.queryForRequestCount("12345");
		verify(mockView).showErrorMessage(anyString());
	}
}
