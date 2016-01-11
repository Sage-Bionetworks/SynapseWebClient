package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void configure();
	}

	void setPresenter(ThreadWidget presenter);

	void addReply(Widget w);

	void clear();
}
