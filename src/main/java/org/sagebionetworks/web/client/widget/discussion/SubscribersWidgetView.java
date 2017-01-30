package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SubscribersWidgetView extends IsWidget {

	public interface Presenter {
		void onClickFollowersLink();
	}

	void setPresenter(SubscribersWidget presenter);
	void clearFollowerCount();
	void setSubscriberCount(Long count);
	void setSubscribersLinkVisible(boolean visible);
	void setSynapseAlert(Widget w);
	void setUserListContainer(Widget w);
	void showDialog();
}
