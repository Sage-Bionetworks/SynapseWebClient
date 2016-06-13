package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
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
		widget = new TeamListWidget(mockView, mockGlobalApplicationState);
		teamList = setupUserTeams(mockSynapseClient);
		AsyncMockStubber.callSuccessWith(0l).when(mockSynapseClient).getOpenRequestCount(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	public static ArrayList<Team> setupUserTeams(SynapseClientAsync mockSynapseClient) throws JSONObjectAdapterException {
		Team testTeam1 = new Team();
		testTeam1.setId("42");
		testTeam1.setName("My Test Team");
		Team testTeam2 = new Team();
		testTeam2.setId("24");
		testTeam2.setName("Team Test My");
		ArrayList<Team> teamList = new ArrayList<Team>();
		teamList.add(testTeam1);
		teamList.add(testTeam2);
		AsyncMockStubber.callSuccessWith(teamList).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		return teamList;
	}
	
	@Test
	public void testConfigureIsBig() {
		//this is the configuration used by the Teams tab in the dashboard
		boolean isBig = true;
		widget.configure(isBig);
		verify(mockView).configure(eq(isBig));
	}
	
	@Test
	public void testConfigureSmall() {
		//this is the configuration used by the old home page
		boolean isBig = false;
		//null request count callback
		widget.configure(isBig);
		verify(mockView).configure(eq(isBig));
	}
	
}
