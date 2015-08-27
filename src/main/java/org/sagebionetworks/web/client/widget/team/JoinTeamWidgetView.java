package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface JoinTeamWidgetView extends IsWidget, SynapseView {
	
	public interface Presenter extends SynapsePresenter, IsWidget {
		public void sendJoinRequest(String message);
		//service may be added later to query for current user requests to allow deletion
//		public void deleteAllJoinRequests();
		void gotoLoginPage();
	}
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void showJoinWizard();
	void hideJoinWizard();
	void showInfo(String title, String message);
	void setButtonsEnabled(boolean enable);	
	public void setIsMemberMessage(String htmlEscape);
	public void setJoinButtonText(String joinButtonText);
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
	void showPostMessageContentAccessRequirement(String url);
}
