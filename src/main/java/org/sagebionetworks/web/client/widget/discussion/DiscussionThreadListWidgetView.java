package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadListWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();
	}

	void setThreadCountAlert(Widget w);
	
	void setPresenter(DiscussionThreadListWidget presenter);

	void addThread(Widget w);
	
	void clear();

	void setAlert(Widget w);

	void setLoadMoreVisibility(boolean visible);

	void setLoadingVisible(boolean visible);

	void setThreadHeaderVisible(boolean visible);

	boolean isLoadMoreAttached();

	boolean isLoadMoreInViewport();

	boolean getLoadMoreVisibility();
}
