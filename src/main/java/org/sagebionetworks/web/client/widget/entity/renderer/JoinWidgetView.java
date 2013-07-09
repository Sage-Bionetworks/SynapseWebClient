package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface JoinWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void configure(WikiPageKey wikiKey, UserEvaluationState state);
	
	void showError(String message);
	
	void showAnonymousRegistrationMessage();
	
	
	void showAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback);
	void showInfo(String title, String message);
	
	void showSubmissionUserGuide();
	
	void showProfileForm(UserProfile profile, AsyncCallback<Void> callback);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		/**
		 * Called when Join button is clicked
		 */
		void register();
		
		void gotoLoginPage();
		
		void submitToChallengeClicked();
		
		void submissionUserGuideSkipped();
	}
}
