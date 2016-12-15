package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ChallengeWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setCreateChallengeVisible(boolean visible);
	void setChallengeTeamWidget(Widget w);
	void setChallengeVisible(boolean visible);
	void setChallengeId(String challengeId);
	void add(Widget w);
	void setSelectTeamModal(Widget w);
	public interface Presenter {
		void onDeleteChallengeClicked();
		void onCreateChallengeClicked();
		void onEditTeamClicked();
	}
}
