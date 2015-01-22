package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface RegisterTeamDialogView extends IsWidget {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setRecruitmentMessage(String message);
	String getRecruitmentMessage();
	void showTeamSelector(boolean isVisible);
	String getSelectedTeam();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void setPreSelectedTeam(String recruitmentMessage, String teamId);
		void okClicked();
	}
}
