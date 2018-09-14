package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface JoinTeamWidgetView extends IsWidget {
	
	public interface Presenter extends IsWidget {
		public void sendJoinRequest(String message);
		void gotoLoginPage();
		void onRequestAccess();
	}
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void showJoinWizard();
	void hideJoinWizard();
	void showInfo(String message);
	void setButtonsEnabled(boolean enable);	
	public void setIsMemberMessage(String htmlEscape);
	public void setJoinButtonsText(String joinButtonText);
	public void setRequestOpenText(String requestOpenText);
	void setUserPanelVisible(boolean isVisible);
	void setRequestMessageVisible(boolean isVisible);
	void setSimpleRequestButtonVisible(boolean isVisible);
	void setRequestButtonVisible(boolean isVisible);
	void setAcceptInviteButtonVisible(boolean isVisible);
	void setAnonUserButtonVisible(boolean isVisible);
	void setIsMemberMessageVisible(boolean isVisible);
	public void setProgressWidget(WizardProgressWidget progressWidget);
	void setCurrentWizardContent(IsWidget isWidget);
	void setJoinWizardCallback(Callback callback);
	void setJoinWizardPrimaryButtonText(String primaryButtonText);
	void setAccessRequirementHTML(String html);
	void setCurrentWizardPanelVisible(boolean isVisible);
	void setSynAlert(IsWidget widget);
	void clear();
	void open(String url);
	void setAccessRequirementsLinkVisible(boolean visible);
}
