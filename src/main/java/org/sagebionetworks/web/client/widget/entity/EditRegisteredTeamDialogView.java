package org.sagebionetworks.web.client.widget.entity;

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
	void showInfo(String message);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onOk();
		void onUnregister();
	}
}
