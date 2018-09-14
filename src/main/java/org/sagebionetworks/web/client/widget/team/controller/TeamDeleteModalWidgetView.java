package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.utils.Callback;

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

	void showInfo(String message);

	void hide();
}
