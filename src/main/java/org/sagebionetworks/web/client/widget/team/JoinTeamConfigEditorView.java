package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.user.client.ui.IsWidget;

public interface JoinTeamConfigEditorView extends IsWidget {

	public interface Presenter extends IsWidget {

	}

	void setIsChallenge(boolean isChallengeSignup);

	void setIsSimpleRequest(boolean isSimpleRequest);

	void setIsMemberMessage(String isMemberMessage);

	void setSuccessMessage(String successMessage);

	void setButtonText(String buttonText);

	void setRequestOpenInfotext(String requestOpenInfoText);

	boolean getIsChallenge();

	boolean getIsSimpleRequest();

	String getIsMemberMessage();

	String getSuccessMessage();

	String getButtonText();

	String getRequestOpenInfotext();

	void setSuggestWidget(IsWidget teamSuggestBox);
}
