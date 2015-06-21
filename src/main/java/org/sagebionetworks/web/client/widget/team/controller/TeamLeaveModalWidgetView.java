package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetView.Presenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamLeaveModalWidgetView {

	Widget asWidget();
	
	public interface Presenter {
		void onConfirm();
		Widget asWidget();
		void setRefreshCallback(Callback refreshCallback);
		void configure(Team team);
	}

	void setSynAlertWidget(Widget asWidget);

	void setPresenter(Presenter presenter);

	void show();

	void showInfo(String title, String message);

	void hide();
	
}
