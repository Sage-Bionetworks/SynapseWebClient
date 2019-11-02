package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorView;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamSelector;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import com.google.gwt.user.client.ui.Widget;

public class UserSelectorTest {

	UserTeamSelector widget;
	@Mock
	UserSelectorView mockView;
	@Mock
	SynapseSuggestBox mockSuggestBox;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	CallbackP<String> mockUsernameCallback;
	@Mock
	UserGroupSuggestion mockSuggestion;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	public static final String SUGGESTION_ID = "Maythe4thBeWithYou";
	public static final String USERNAME = "Y0da";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		widget = new UserTeamSelector(mockView, mockSuggestBox, mockUserGroupSuggestionProvider, mockGWT);
		widget.configure(mockUsernameCallback);

		when(mockSuggestion.getId()).thenReturn(SUGGESTION_ID);
		when(mockSuggestion.getHeader()).thenReturn(mockUserGroupHeader);
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(true);
		when(mockUserGroupHeader.getUserName()).thenReturn(USERNAME);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockSuggestBox).setSuggestionProvider(mockUserGroupSuggestionProvider);
		verify(mockSuggestBox).setTypeFilter(TypeFilter.ALL);
		verify(mockView).setSelectBox(any(Widget.class));
		verify(mockSuggestBox).addItemSelectedHandler(any(CallbackP.class));
	}

	@Test
	public void testOnSynapseSuggestSelected() {
		widget.onSynapseSuggestSelected(mockSuggestion);
		verify(mockGWT).scheduleDeferred(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockUsernameCallback).invoke(USERNAME);
		verify(mockView).hide();
	}

	@Test
	public void testOnSynapseSuggestTeamSelected() {
		String teamName = "My Team";
		String teamAlias = "MyTeam";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(false);
		when(mockUserGroupHeader.getUserName()).thenReturn(teamName);
		when(mockGWT.getUniqueAliasName(teamName)).thenReturn(teamAlias);
		widget.onSynapseSuggestSelected(mockSuggestion);

		verify(mockGWT).scheduleDeferred(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockUsernameCallback).invoke(teamAlias);
		verify(mockView).hide();
	}

	@Test
	public void testOnModalShown() {
		widget.onModalShown();
		verify(mockSuggestBox).setFocus(true);
	}

	@Test
	public void testShow() {
		widget.show();
		verify(mockSuggestBox).clear();
		verify(mockView).show();
	}


	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
