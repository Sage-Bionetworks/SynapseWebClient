package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.widget.entity.editor.TeamSelectEditor;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

@RunWith(MockitoJUnitRunner.class)
public class TeamSelectEditorTest {

	TeamSelectEditor widget;
	@Mock
	SynapseSuggestBox mockTeamSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	UserGroupSuggestion mockGroupSuggestion;
	@Mock
	HashMap<String, String> mockDescriptor;
	@Mock
	WikiPageKey mockWikiPageKey;
	public static final String SELECTED_TEAM_ID = "987654";

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TeamSelectEditor(mockTeamSuggestBox, mockProvider);

		when(mockGroupSuggestion.getId()).thenReturn(SELECTED_TEAM_ID);
		when(mockTeamSuggestBox.getSelectedSuggestion()).thenReturn(mockGroupSuggestion);
	}

	@Test
	public void testConstruction() {
		verify(mockTeamSuggestBox).setSuggestionProvider(mockProvider);
		verify(mockTeamSuggestBox).setTypeFilter(TypeFilter.TEAMS_ONLY);
	}

	@Test
	public void testHappyCase() {
		widget.configure(mockWikiPageKey, mockDescriptor, null);
		widget.updateDescriptorFromView();

		verify(mockDescriptor).put(WidgetConstants.TEAM_ID_KEY, SELECTED_TEAM_ID);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNoTeamSelected() {
		when(mockTeamSuggestBox.getSelectedSuggestion()).thenReturn(null);
		widget.configure(mockWikiPageKey, mockDescriptor, null);
		widget.updateDescriptorFromView();
	}

	@Test
	public void testClear() {
		widget.clearState();
		verify(mockTeamSuggestBox).clear();
	}
}
