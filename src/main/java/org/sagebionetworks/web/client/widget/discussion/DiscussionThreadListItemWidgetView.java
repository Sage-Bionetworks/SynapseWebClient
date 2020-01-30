package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadListItemWidgetView extends IsWidget {

	public interface Presenter {

		Widget asWidget();

		void onClickThread();
	}

	void setPresenter(DiscussionThreadListItemWidget presenter);

	void setTitle(String title);

	void setNumberOfReplies(String numberOfReplies);

	void setNumberOfViews(String numberOfViews);

	void setLastActivity(String lastActivity);

	void addActiveAuthor(Widget user);

	void setThreadAuthor(Widget widget);

	void setPinnedIconVisible(boolean visible);

	void clearActiveAuthors();

	void setThreadUrl(String url);
}
