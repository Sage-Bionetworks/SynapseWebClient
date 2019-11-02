package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamConfigEditor implements WidgetEditorPresenter, JoinTeamConfigEditorView.Presenter {

	private JoinTeamConfigEditorView view;
	private Map<String, String> descriptor;
	private SynapseSuggestBox teamSuggestBox;
	private SynapseJavascriptClient jsClient;
	private UserGroupSuggestionProvider provider;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public JoinTeamConfigEditor(JoinTeamConfigEditorView view, SynapseSuggestBox teamSuggestBox, UserGroupSuggestionProvider provider, SynapseJavascriptClient jsClient, SynapseJSNIUtils jsniUtils) {
		this.provider = provider;
		this.jsClient = jsClient;
		this.teamSuggestBox = teamSuggestBox;
		this.jsniUtils = jsniUtils;
		teamSuggestBox.setSuggestionProvider(provider);
		teamSuggestBox.setTypeFilter(TypeFilter.TEAMS_ONLY);
		this.view = view;
		this.view.setSuggestWidget(teamSuggestBox);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}


	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback window) {
		this.descriptor = widgetDescriptor;
		if (descriptor.containsKey(WidgetConstants.TEAM_ID_KEY)) {
			jsClient.getTeam(descriptor.get(WidgetConstants.TEAM_ID_KEY), new AsyncCallback<Team>() {
				@Override
				public void onFailure(Throwable caught) {
					jsniUtils.consoleError(caught.getMessage());
				}

				@Override
				public void onSuccess(Team team) {
					UserGroupHeader ugh = new UserGroupHeader();
					ugh.setDisplayName(team.getName());
					ugh.setIsIndividual(false);
					ugh.setOwnerId(team.getId());
					UserGroupSuggestion suggestion = new UserGroupSuggestion(ugh, team.getName(), teamSuggestBox.getWidth());
					teamSuggestBox.setSelectedSuggestion(suggestion);
					teamSuggestBox.setText(team.getName());
				}

			});
		}
		// is the team associated with joining a challenge?
		boolean isChallengeSignup = false;
		if (descriptor.containsKey(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY)) {
			isChallengeSignup = Boolean.parseBoolean(descriptor.get(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY));
		} else {
			// check for old param
			isChallengeSignup = descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY) ? Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY)) : false;
		}
		view.setIsChallenge(isChallengeSignup);
		boolean isSimpleRequest = false;
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON)) {
			isSimpleRequest = Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON));
		}
		view.setIsSimpleRequest(isSimpleRequest);
		String isMemberMessage = "Already a member";
		if (descriptor.containsKey(WidgetConstants.IS_MEMBER_MESSAGE)) {
			isMemberMessage = descriptor.get(WidgetConstants.IS_MEMBER_MESSAGE);
		}
		view.setIsMemberMessage(isMemberMessage);
		String successMessage = WidgetConstants.JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE;
		if (descriptor.containsKey(WidgetConstants.SUCCESS_MESSAGE)) {
			successMessage = descriptor.get(WidgetConstants.SUCCESS_MESSAGE);
		}
		view.setSuccessMessage(successMessage);
		String buttonText = "Join";
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_BUTTON_TEXT)) {
			buttonText = descriptor.get(WidgetConstants.JOIN_TEAM_BUTTON_TEXT);
		}
		view.setButtonText(buttonText);
		String requestOpenInfoText = "Your request to join this team has been sent.";
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT)) {
			requestOpenInfoText = descriptor.get(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT);
		}
		view.setRequestOpenInfotext(requestOpenInfoText);
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		UserGroupSuggestion suggestion = (UserGroupSuggestion) teamSuggestBox.getSelectedSuggestion();
		if (suggestion != null) {
			descriptor.put(WidgetConstants.TEAM_ID_KEY, suggestion.getId());
		}
		if (descriptor.containsKey(WidgetConstants.TEAM_ID_KEY)) {
			descriptor.put(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY, String.valueOf(view.getIsChallenge()));
			descriptor.put(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON, String.valueOf(view.getIsSimpleRequest()));
			descriptor.put(WidgetConstants.IS_MEMBER_MESSAGE, view.getIsMemberMessage());
			descriptor.put(WidgetConstants.SUCCESS_MESSAGE, view.getSuccessMessage());
			descriptor.put(WidgetConstants.JOIN_TEAM_BUTTON_TEXT, view.getButtonText());
			descriptor.put(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT, view.getRequestOpenInfotext());
		} else {
			throw new IllegalArgumentException("Please select a team.");
		}

	}

	@Override
	public String getTextToInsert() {
		return null;
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}

	// for testing only
	public void setDescriptor(Map<String, String> widgetDescriptor) {
		this.descriptor = widgetDescriptor;
	}

}
