package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface JoinWidgetView extends IsWidget {

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
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		/**
		 * Called when Join button is clicked
		 */
		public void register();
		
		public void gotoLoginPage();
	}
}
