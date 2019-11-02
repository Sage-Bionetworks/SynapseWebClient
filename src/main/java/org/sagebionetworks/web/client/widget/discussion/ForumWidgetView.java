package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ForumWidgetView extends IsWidget {

	public interface Presenter {
		void onClickShowAllThreads();

		Widget asWidget();

		void onSortReplies(boolean ascending);

		void onClickNewThread();
	}

	void setPresenter(Presenter presenter);

	void setThreadList(Widget w);

	void setSingleThread(Widget w);

	void setNewThreadModal(Widget w);

	void setAlert(Widget w);

	void setSubscribeButton(Widget w);

	void showErrorMessage(String errorMessage);

	void setSingleThreadUIVisible(boolean visible);

	void setThreadListUIVisible(boolean visible);

	void setNewThreadButtonVisible(boolean visible);

	void setShowAllThreadsButtonVisible(boolean visible);

	void setSortRepliesButtonVisible(boolean visible);

	void setDefaultThreadWidget(Widget w);

	void setDefaultThreadWidgetVisible(boolean visible);

	boolean isDeletedThreadListVisible();

	void setDeletedThreadListVisible(boolean visible);

	void setDeletedThreadList(Widget widget);

	void setMainContainerVisible(boolean visible);

	void setSubscribersWidget(Widget w);

	void setSubscribersWidgetVisible(boolean visible);
}
