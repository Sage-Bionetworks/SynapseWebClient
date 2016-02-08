package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadListWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();
	}

	void setPresenter(DiscussionThreadListWidget presenter);

	void addThread(Widget w);

	void clear();

	void setAlert(Widget w);

	void setLoadMoreButtonVisibility(boolean visible);

	void setEmptyUIVisible(boolean visible);

	void setThreadHeaderVisible(boolean visible);

	void setLoadingVisible(boolean visible);
}
