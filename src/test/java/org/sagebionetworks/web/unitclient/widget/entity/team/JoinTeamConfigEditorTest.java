package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditor;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditorView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JoinTeamConfigEditorTest {

	JoinTeamConfigEditor presenter;
	JoinTeamConfigEditorView mockView;
	SynapseSuggestBox mockSuggestBox;
	UserGroupSuggestionProvider mockProvider;
	DialogCallback mockCallback;
	UserGroupSuggestion mockSuggestion;
	SynapseJSNIUtils mockJSNI;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	String teamID = "123123";
	String suggestionID = "987987";
	boolean isSimpleRequest = true;
	String isMemberMessage = "Already a member!";
	String successMessage = "Joined the team successfully!";
	String joinTeamButtonText = "Join this team!";
	String openRequestText = "Request to join team sent!";
	Team testTeam;
	String teamName = "testName";

	// challenge params
	boolean isChallenge = true; // current
	boolean showProfileFormKey = true; // deprecated but needs support

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(JoinTeamConfigEditorView.class);
		mockSuggestion = mock(UserGroupSuggestion.class);
		mockSuggestBox = mock(SynapseSuggestBox.class);
		mockProvider = mock(UserGroupSuggestionProvider.class);
		mockCallback = mock(DialogCallback.class);
		mockJSNI = mock(SynapseJSNIUtils.class);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, teamID);
		descriptor.put(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY, String.valueOf(isChallenge));
		descriptor.put(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY, String.valueOf(showProfileFormKey));
		descriptor.put(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON, String.valueOf(isSimpleRequest));
		descriptor.put(WidgetConstants.IS_MEMBER_MESSAGE, isMemberMessage);
		descriptor.put(WidgetConstants.SUCCESS_MESSAGE, successMessage);
		descriptor.put(WidgetConstants.JOIN_TEAM_BUTTON_TEXT, joinTeamButtonText);
		descriptor.put(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT, openRequestText);
		testTeam = new Team();
		testTeam.setId(teamID);
		testTeam.setName(teamName);
		presenter = new JoinTeamConfigEditor(mockView, mockSuggestBox, mockProvider, mockSynapseJavascriptClient, mockJSNI);
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getId()).thenReturn(suggestionID);
		AsyncMockStubber.callSuccessWith(testTeam).when(mockSynapseJavascriptClient).getTeam(Mockito.anyString(), Mockito.any(AsyncCallback.class));
	}

	@Test
	public void testAsWidget() {
		presenter.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConstruction() {
		verify(mockSuggestBox).setSuggestionProvider(mockProvider);
		verify(mockSuggestBox).setTypeFilter(TypeFilter.TEAMS_ONLY);
		verify(mockView).setSuggestWidget(mockSuggestBox);
	}

	@Test
	public void testConfigure() {
		presenter.configure(wikiKey, descriptor, mockCallback);
		verify(mockSuggestBox).setText(teamName);
		verify(mockView).setIsChallenge(isChallenge);
		verify(mockView).setIsSimpleRequest(isSimpleRequest);
		verify(mockView).setIsMemberMessage(isMemberMessage);
		verify(mockView).setSuccessMessage(successMessage);
		verify(mockView).setButtonText(joinTeamButtonText);
		verify(mockView).setRequestOpenInfotext(openRequestText);
	}

	@Test
	public void updateDescriptorFromViewSuccess() {
		presenter.setDescriptor(descriptor);
		presenter.updateDescriptorFromView();
		verify(mockSuggestBox).getSelectedSuggestion();
		verify(mockSuggestion).getId();
		verify(mockView).getIsChallenge();
		verify(mockView).getIsSimpleRequest();
		verify(mockView).getIsMemberMessage();
		verify(mockView).getSuccessMessage();
		verify(mockView).getButtonText();
		verify(mockView).getRequestOpenInfotext();
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateDescriptorFromViewNoTeamSelected() {
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		descriptor = new HashMap<String, String>();
		presenter.setDescriptor(descriptor);
		presenter.updateDescriptorFromView();
		verify(mockSuggestBox, Mockito.never()).getSelectedSuggestion();
	}
}
