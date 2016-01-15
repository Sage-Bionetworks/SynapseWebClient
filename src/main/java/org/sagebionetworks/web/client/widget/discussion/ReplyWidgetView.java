package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ReplyWidgetView extends IsWidget{

	public interface Presenter {

		void configure(DiscussionReplyBundle bundle);

		Widget asWidget();
	}

	void setPresenter(ReplyWidget presenter);
}
