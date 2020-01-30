package org.sagebionetworks.web.client.widget.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TopicRowWidgetView extends IsWidget {
	void setTopicWidget(Widget w);

	void setSubscribeButtonWidget(Widget w);
}
