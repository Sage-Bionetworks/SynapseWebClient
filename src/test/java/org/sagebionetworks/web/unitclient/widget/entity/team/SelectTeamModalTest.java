package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.client.widget.team.SelectTeamModalView;
import com.google.gwt.user.client.ui.Widget;

public class SelectTeamModalTest {

	SelectTeamModal widget;
	@Mock
	SelectTeamModalView mockView;
	@Mock
	SynapseSuggestBox mockTeamSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	UserGroupSuggestion mockGroupSuggestion;
	@Mock
	CallbackP<String> mockTeamIdSelectedCallback;
	public static final String PARTICIPANT_TEAM_ID = "1234567890";
	public static final String CHALLENGE_ID = "45678";
	public static final String SELECTED_TEAM_ID = "987654";

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SelectTeamModal(mockView, mockTeamSuggestBox, mockProvider);

		when(mockGroupSuggestion.getId()).thenReturn(SELECTED_TEAM_ID);
		when(mockTeamSuggestBox.getSelectedSuggestion()).thenReturn(mockGroupSuggestion);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSuggestWidget(any(Widget.class));
		verify(mockTeamSuggestBox).setSuggestionProvider(mockProvider);
		verify(mockTeamSuggestBox).setTypeFilter(TypeFilter.TEAMS_ONLY);
	}

	@Test
	public void testHappyCase() {
		widget.configure(mockTeamIdSelectedCallback);
		widget.show();
		verify(mockTeamSuggestBox).clear();
		verify(mockView).show();

		// simulate that a team was selected
		widget.onSelectTeam();
		verify(mockTeamIdSelectedCallback).invoke(SELECTED_TEAM_ID);
	}


	@Test
	public void testNoTeamSelected() {
		when(mockTeamSuggestBox.getSelectedSuggestion()).thenReturn(null);
		widget.configure(mockTeamIdSelectedCallback);
		widget.show();
		verify(mockTeamSuggestBox).clear();
		verify(mockView).show();

		widget.onSelectTeam();
		verify(mockTeamIdSelectedCallback, never()).invoke(SELECTED_TEAM_ID);
	}


	@Test
	public void testNotConfigured() {
		widget.show();
		verify(mockTeamSuggestBox).clear();
		verify(mockView).show();

		widget.onSelectTeam();
		verify(mockTeamIdSelectedCallback, never()).invoke(SELECTED_TEAM_ID);
	}
}
