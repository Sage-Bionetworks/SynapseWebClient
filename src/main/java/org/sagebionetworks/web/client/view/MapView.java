package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface MapView extends IsWidget {
	void setTeamBadge(Widget w);

	void setMap(Widget w);

	void setTeamBadgeVisible(boolean visible);

	void setAllUsersTitleVisible(boolean visible);

	int getClientHeight();

	void setPresenter(Presenter presenter);

	public interface Presenter {
	}
}
