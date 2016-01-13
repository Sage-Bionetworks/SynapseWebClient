package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ThreadListWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void configure(String forumId);
	}

	void setPresenter(ThreadListWidget presenter);

	void addThread(Widget w);

	void clear();
}
