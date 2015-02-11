package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.user.client.ui.IsWidget;

public interface ChallengeBadgeView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setChallenge(ChallengeBundle header);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onClick();
		void onParticipantsClick();
	}

	

}
