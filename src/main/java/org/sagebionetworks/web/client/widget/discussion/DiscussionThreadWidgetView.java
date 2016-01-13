package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void configure(DiscussionThreadBundle bundle);
	}

	void setPresenter(DiscussionThreadWidget presenter);

	void addReply(Widget w);

	void clear();

	void setTitle(String title);

	void setMessage(String message);

	void setActiveUsers(String activeAuthors);

	void setNumberOfReplies(String numberOfReplies);

	void setNumberOfViews(String numberOfViews);

	void setLastActivity(String lastActivity);
}
