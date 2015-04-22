package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;
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
		AsyncMockStubber.callSuccessWith(0l).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	public static ArrayList<Team> setupUserTeams(JSONObjectAdapter adapter, SynapseClientAsync mockSynapseClient) throws JSONObjectAdapterException {
		Team testTeam = new Team();
		testTeam.setId("42");
		testTeam.setName("My Test Team");
		ArrayList<Team> teamList = new ArrayList<Team>();
		teamList.add(testTeam);
		AsyncMockStubber.callSuccessWith(teamList).when(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		return teamList;
	}
	
	@Test
	public void testGetTeams() throws Exception {
		widget.getTeams("12345",mockSynapseClient, adapterFactory, mockGetTeamsCallback);
		verify(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		verify(mockGetTeamsCallback).onSuccess(eq(teamList));
	}
	@Test
	public void testGetTeamsFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		widget.getTeams("12345",mockSynapseClient, adapterFactory, mockGetTeamsCallback);
		verify(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		verify(mockGetTeamsCallback).onFailure(eq(ex));
	}

	
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
	
	@Test
	public void testGetQueryForRequestCountFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		widget.queryForRequestCount("12345");
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureIsBigNoRequestCount() {
		//this is the configuration used by the Team Search page
		boolean isBig = true;
		boolean isRequestCountVisible = false;
		widget.configure(teamList, isBig, isRequestCountVisible);
		verify(mockView).configure(eq(teamList), eq(isBig));
		//should not call since request count is not visible
		verify(mockSynapseClient, never()).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureIsBigWithRequestCount() {
		//this is the configuration used by the Teams tab in the dashboard
		boolean isBig = true;
		boolean isRequestCountVisible = true;
		//also test the request count callback
		TeamListWidget.RequestCountCallback mockCallback = mock(TeamListWidget.RequestCountCallback.class);
		widget.configure(teamList, isBig, isRequestCountVisible, mockCallback);
		verify(mockView).configure(eq(teamList), eq(isBig));
		verify(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
		//verify callback was invoked
		verify(mockCallback).invoke(anyString(), anyLong());
	}
	
	@Test
	public void testConfigureSmallWithRequestCount() {
		//this is the configuration used by the old home page
		boolean isBig = false;
		boolean isRequestCountVisible = true;
		//null request count callback
		widget.configure(teamList, isBig, isRequestCountVisible, null);
		verify(mockView).configure(eq(teamList), eq(isBig));
		verify(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
	}
	
}
