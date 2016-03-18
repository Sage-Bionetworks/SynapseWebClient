package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TopicRowWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setTopicWidget(Widget w);
	void setSubscribeButtonWidget(Widget w);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
