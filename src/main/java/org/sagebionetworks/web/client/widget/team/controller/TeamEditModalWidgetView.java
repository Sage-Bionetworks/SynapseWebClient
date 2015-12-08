package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetView.Presenter;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamEditModalWidgetView {

	public interface Presenter {
		public Widget asWidget();
		void setRefreshCallback(Callback refreshCallback);
		void onConfirm();
		void hide();
	}

	public Widget asWidget();
	public void setAlertWidget(Widget asWidget);
	void setPresenter(Presenter presenter);
	String getName();
	String getDescription();
	boolean getPublicJoin();
	void setUploadWidget(Widget uploader);
	void setImageURL(String fileHandleId);
	void setDefaultIconVisible();
	void setAuthenticatedUsersCanSendMessageToTeam(boolean canSendMessage);
	boolean canAuthenticatedUsersSendMessageToTeam();
	void showInfo(String title, String message);
	void show();
	void hide();
	void showLoading();
	void hideLoading();
	void clear();
	void configure(Team team);
}
