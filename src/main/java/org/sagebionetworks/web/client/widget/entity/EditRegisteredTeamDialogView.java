package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ShowsErrors;

import com.google.gwt.user.client.ui.IsWidget;

public interface EditRegisteredTeamDialogView extends IsWidget, ShowsErrors {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setRecruitmentMessage(String message);
	String getRecruitmentMessage();
	void showModal();
	void hideModal();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onOk();
		void onUnregister();
	}
}
