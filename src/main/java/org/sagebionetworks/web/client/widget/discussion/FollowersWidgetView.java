package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FollowersWidgetView extends IsWidget {

	public interface Presenter {
		void onClickFollowersLink();
	}

	void setPresenter(FollowersWidget presenter);
	void clearFollowerCount();
	void setFollowerCount(int count);
	void setFollowersLinkVisible(boolean visible);
	void setSynapseAlert(Widget w);
	void setUserListContainer(Widget w);
	void showDialog();
}
