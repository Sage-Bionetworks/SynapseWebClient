package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ForumWidgetView extends IsWidget{

	public interface Presenter {
		void onClickNewThread();
		void onClickShowAllThreads();
		Widget asWidget();
		void onClickDeletedThreadButton();
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
	void setDefaultThreadWidget(Widget w);
	void setDefaultThreadWidgetVisible(boolean visible);
	void showNewThreadTooltip();
	boolean isDeletedThreadListVisible();
	void setDeletedThreadListVisible(boolean visible);
	void setDeletedThreadList(Widget widget);
	void setDeletedThreadButtonVisible(boolean visible);
	void setDeletedThreadButtonIcon(IconType icon);
}
