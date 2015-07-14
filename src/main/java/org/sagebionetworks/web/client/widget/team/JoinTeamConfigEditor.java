package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamConfigEditor implements WidgetEditorPresenter, JoinTeamConfigEditorView.Presenter {

	private JoinTeamConfigEditorView view;
	private Map<String, String> descriptor;

	@Inject
	public JoinTeamConfigEditor(JoinTeamConfigEditorView view) {
		this.view = view;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor, DialogCallback window) {
		this.descriptor = widgetDescriptor;		
		String teamId = "";
		if (descriptor.containsKey(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY)) {
			teamId = descriptor.get(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY);
		}
		view.setTeamId(teamId);
		//is the team associated with joining a challenge?
		boolean isChallengeSignup = false;
		if (descriptor.containsKey(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY)) {
			isChallengeSignup = Boolean.parseBoolean(descriptor.get(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY));
		} else {
			//check for old param
			isChallengeSignup = descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY) ? 
					Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY)) : false;
		}
		view.setIsChallenge(isChallengeSignup);
		boolean isSimpleRequest = false;
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON)) {
			isSimpleRequest = Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON));
		}
		view.setIsSimpleRequest(isSimpleRequest);
		String isMemberMessage = "Successfully joined";
		if (descriptor.containsKey(WidgetConstants.IS_MEMBER_MESSAGE)) {
			isMemberMessage =descriptor.get(WidgetConstants.IS_MEMBER_MESSAGE);
		}
		view.setIsMemberMessage(isMemberMessage);
		String successMessage = WidgetConstants.JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE;
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE)) {
			successMessage = descriptor.get(WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE);
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
	public void checkParams() {
		// validate team with synapseClient.isTeam or something 
		// validate button text to be non-empty
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		checkParams();
		descriptor.put(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY, view.getTeamId());
		descriptor.put(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY, String.valueOf(view.getIsChallenge()));
		descriptor.put(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON, String.valueOf(view.getIsSimpleRequest()));
		descriptor.put(WidgetConstants.IS_MEMBER_MESSAGE, view.getIsMemberMessage());
		descriptor.put(WidgetConstants.JOIN_TEAM_SUCCESS_MESSAGE, view.getSuccessMessage());
		descriptor.put(WidgetConstants.JOIN_TEAM_BUTTON_TEXT, view.getButtonText());
		descriptor.put(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT, view.getRequestOpenInfotext());
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

}
