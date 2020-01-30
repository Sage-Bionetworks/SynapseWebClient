package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TeamSearchPresenterTest {

	TeamSearchPresenter presenter;

	@Mock
	TeamSearchView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	UserAccountServiceAsync mockUserAccountServiceAsync;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseClientAsync mockSynapse;
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	TeamSearch mockPlace;
	@Mock
	BigTeamBadge mockTeamBadge;
	PaginatedResults<Team> teamList = getTestTeams();
	String searchTerm = "test";

	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		presenter = new TeamSearchPresenter(mockView, mockGlobalApplicationState, mockSynapse, mockCookies, mockSynAlert, mockLoadMoreWidgetContainer, mockPortalGinInjector);

		AsyncMockStubber.callSuccessWith(teamList).when(mockSynapse).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));

		verify(mockView).setPresenter(presenter);
		when(mockPlace.getSearchTerm()).thenReturn(searchTerm);
		when(mockPortalGinInjector.getBigTeamBadgeWidget()).thenReturn(mockTeamBadge);
	}

	private static PaginatedResults<Team> getTestTeams() {
		PaginatedResults<Team> teams = new PaginatedResults<Team>();

		List<Team> teamList = new ArrayList<Team>();
		Team team = new Team();
		team.setId("42");
		team.setName("Springfield Isotopes");
		team.setDescription("Springfield's only minor league baseball team.");
		teamList.add(team);
		team = new Team();
		team.setId("43");
		team.setName("Rogue Squadron");
		team.setDescription("We need you.");
		teamList.add(team);
		teams.setResults(teamList);
		teams.setTotalNumberOfResults(teamList.size());
		return teams;
	}

	@Test
	public void testSearch() throws RestServiceException {
		presenter.setPlace(mockPlace);
		verify(mockView).setSearchTerm(searchTerm);
		// add both test teams
		verify(mockPortalGinInjector, times(2)).getBigTeamBadgeWidget();
		verify(mockLoadMoreWidgetContainer, times(2)).add(any(Widget.class));
	}

	@Test
	public void testSearchFailure() throws RestServiceException {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapse).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).handleException(caught);
	}

	@Test
	public void testCanPublicJoin() throws RestServiceException {
		// can public join is interpretted as false if null
		Team team = new Team();
		team.setCanPublicJoin(null);
		assertFalse(TeamSearchPresenter.getCanPublicJoin(team));
		team.setCanPublicJoin(false);
		assertFalse(TeamSearchPresenter.getCanPublicJoin(team));

		team.setCanPublicJoin(true);
		assertTrue(TeamSearchPresenter.getCanPublicJoin(team));
	}

	@Test
	public void testEmptyTeams() {
		PaginatedResults<Team> teams = new PaginatedResults<Team>();
		teams.setResults(new ArrayList<Team>());
		teams.setTotalNumberOfResults(0);
		AsyncMockStubber.callSuccessWith(teams).when(mockSynapse).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		presenter.setPlace(mockPlace);
		verify(mockSynapse).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
}
