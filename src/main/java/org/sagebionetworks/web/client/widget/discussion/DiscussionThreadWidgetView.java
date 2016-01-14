package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void toggle();
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

	void toggle();

	void setAuthor(String author);

	void setCreatedOn(String createdOn);
}
