package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionListWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void configure();
	}

	void setPresenter(DiscussionListWidget presenter);

	void addThread(Widget w);

	void clear();
}
