package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.place.MapPlace;
import org.sagebionetworks.web.client.presenter.MapPresenter;
import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import com.google.gwt.user.client.ui.Widget;

public class MapPresenterTest {
	MapPresenter presenter;
	@Mock
	MapView mockView;
	@Mock
	GoogleMap mockMap;
	@Mock
	TeamBadge mockTeamBadge;
	@Mock
	MapPlace mockPlace;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		presenter = new MapPresenter(mockView, mockMap, mockTeamBadge);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setMap(any(Widget.class));
		verify(mockView).setTeamBadge(any(Widget.class));
	}

	@Test
	public void testSetPlaceAllUsers() {
		when(mockPlace.getTeamId()).thenReturn(MapPresenter.ALL_USERS);
		presenter.setPlace(mockPlace);
		verify(mockMap).configure();
		verify(mockView).setAllUsersTitleVisible(true);
		verify(mockView).setTeamBadgeVisible(false);

		// besides setting the team badge in the view, there should be no interactions
		verify(mockTeamBadge).asWidget();
		verifyNoMoreInteractions(mockTeamBadge);
	}

	@Test
	public void testSetPlaceTeam() {
		String teamId = "12345";
		when(mockPlace.getTeamId()).thenReturn(teamId);
		presenter.setPlace(mockPlace);
		verify(mockMap).configure(teamId);
		verify(mockView).setAllUsersTitleVisible(false);
		verify(mockView).setTeamBadgeVisible(true);
		verify(mockTeamBadge).configure(teamId);
	}
}
