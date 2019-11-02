package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SelectTeamModalView extends IsWidget {

	public void setPresenter(Presenter presenter);

	public interface Presenter {
		void onSelectTeam();
	}

	void setSuggestWidget(Widget suggestWidget);

	void show();

	void hide();

	void setTitle(String title);
}
