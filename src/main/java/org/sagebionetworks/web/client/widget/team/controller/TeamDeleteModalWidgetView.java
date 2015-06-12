package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidgetView.Presenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamDeleteModalWidgetView extends IsWidget {

	public interface Presenter {

		void setRefreshCallback(Callback refreshCallback);

		void onConfirm();

		void showDialog();

		void configure(Team team);
		
	}

	void setSynAlertWidget(Widget asWidget);

	Widget asWidget();

	void show();

	void setPresenter(Presenter presenter);

	void showInfo(String title, String message);

	void hide();
}
