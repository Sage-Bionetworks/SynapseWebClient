package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
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
	void setTeams(List<Team> teams);
	void showModal();
	void hideModal();
	void setNoTeamsFoundVisible(boolean isVisible);
	void showConfirmDialog(String message, ConfirmCallback okCallback);
	void showInfo(String title, String message);
	void setNewTeamLink(String url);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void teamSelected(int selectedIndex);
		void onOk();
	}

}
