package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadListWidgetView extends IsWidget {

	public interface Presenter {

		Widget asWidget();
	}

	void setThreadCountAlert(Widget w);

	void setPresenter(DiscussionThreadListWidget presenter);

	void setAlert(Widget w);

	void setThreadHeaderVisible(boolean visible);

	void setNoThreadsFoundVisible(boolean visible);

	void setThreadsContainer(IsWidget container);

	void scrollIntoView(Widget w);

	void setSorted(DiscussionThreadOrder column, boolean ascending);

	void clearSort();
}
