package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SubmitToEvaluationWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void configure(WikiPageKey wikiKey, String buttonText);
	void showUnavailable(String message);
	void showAnonymousRegistrationMessage();
	
	void showInfo(String title, String message);
	void setEvaluationSubmitterWidget(Widget widget);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void gotoLoginPage();
		
		void submitToChallengeClicked();
	}
}
