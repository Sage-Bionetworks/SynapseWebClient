package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SubscriptionView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);
	void setSynAlert(Widget w);
	void setTopicWidget(Widget w);
	void selectFollow();
	void selectUnfollow();
	public interface Presenter extends SynapsePresenter {
		void onFollow();
		void onUnfollow();
	}
}
