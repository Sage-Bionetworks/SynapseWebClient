package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SubscriptionListWidgetView extends IsWidget {

	void setPresenter(Presenter presenter);
	void addNewSubscription(Widget topicRow);
	void setSynAlert(Widget w);
	void setMoreButtonVisible(boolean visible);
	void clearSubscriptions();
	void clearFilter();
	void setNoItemsMessageVisible(boolean visible);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onMore();
		void onFilter(SubscriptionObjectType type);
	}

}
