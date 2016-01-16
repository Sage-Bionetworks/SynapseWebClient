package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view
			) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public void configure(DiscussionReplyBundle bundle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
