package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.SortDirection;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SubscriptionListWidgetView extends IsWidget {

	void setPresenter(Presenter presenter);

	void addNewSubscription(Widget topicRow);

	void setSynAlert(Widget w);

	void clearSubscriptions();

	void clearFilter();

	void setNoItemsMessageVisible(boolean visible);

	void setPagination(Widget w);

	void setLoadingVisible(boolean visible);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onFilter(SubscriptionObjectType type);

		void onSort(SortDirection sortDirection);
	}
}
