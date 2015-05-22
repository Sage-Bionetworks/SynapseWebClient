package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface JoinTeamWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(boolean isLoggedIn,TeamMembershipStatus teamMembershipStatus, String isMemberMessage, String buttonText, String requestOpenInfoText, boolean isSimpleRequestButton);
	void showJoinWizard();
	void hideJoinWizard();
	
	void updateWizardProgress(int currentPage, int totalPages);
	
	void showChallengeInfoPage(UserProfile profile, WikiPageKey challengeInfoWikiPageKey, Callback callback);
	
	void showWikiAccessRequirement(
			Widget wikiPageWidget, 
			Callback touAcceptanceCallback);
	
	void showTermsOfUseAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback);
	
	void showACTAccessRequirement(
			String arText,
			final Callback callback);
	
	void showPostMessageContentAccessRequirement(String url, final Callback touAcceptanceCallback);
	void showInfo(String title, String message);
	
	void setButtonsEnabled(boolean enable);
	
	public interface Presenter extends SynapsePresenter {
		public void sendJoinRequest(String message, boolean isAcceptingInvite);
		//service may be added later to query for current user requests to allow deletion
//		public void deleteAllJoinRequests();
		void gotoLoginPage();
	}
}
