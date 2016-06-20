package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadListItemWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void onClickThread();
	}

	void setPresenter(DiscussionThreadListItemWidget presenter);

	void clear();

	void setTitle(String title);

	void setNumberOfReplies(String numberOfReplies);

	void setNumberOfViews(String numberOfViews);

	void setLastActivity(String lastActivity);

	void setAlert(Widget w);

	void addActiveAuthor(Widget user);

	void showErrorMessage(String errorMessage);

	void showSuccess(String successTitle, String successMessage);

	void setThreadAuthor(Widget widget);

	void setPinnedIconVisible(boolean visible);
}
