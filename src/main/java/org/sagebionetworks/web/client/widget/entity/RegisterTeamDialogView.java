package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ShowsErrors;

import com.google.gwt.user.client.ui.IsWidget;

public interface RegisterTeamDialogView extends IsWidget, ShowsErrors {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setRecruitmentMessage(String message);
	String getRecruitmentMessage();
	void showTeamSelector(boolean isVisible);
	void showUnregisterButton(boolean isVisible);
	void clearTeams();
	void setTeams(List<Team> teams);
	void showModal();
	void hideModal();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void setPreSelectedTeam(String recruitmentMessage, String teamId);
		void teamSelected(String teamName);
		void onOk();
		void onUnregister();
	}
}
