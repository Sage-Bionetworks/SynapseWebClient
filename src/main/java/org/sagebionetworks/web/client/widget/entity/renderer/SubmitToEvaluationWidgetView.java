package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface SubmitToEvaluationWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void configure(WikiPageKey wikiKey, boolean isAvailableEvaluation, String unavailableMessage);
	
	void showError(String message);
	
	void showAnonymousRegistrationMessage();
	
	void showInfo(String title, String message);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void gotoLoginPage();
		
		void submitToChallengeClicked();
	}
}
