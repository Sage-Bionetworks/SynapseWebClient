package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadWidget implements DiscussionThreadWidgetView.Presenter{

	DiscussionThreadWidgetView view;
	PortalGinInjector ginInjector;

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
			PortalGinInjector ginInjector
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(DiscussionThreadBundle bundle) {
		view.clear();
		view.setTitle(bundle.getTitle());
		view.setMessage(bundle.getMessageUrl());
		view.setActiveUsers(bundle.getActiveAuthors().toString());
		view.setNumberOfReplies(bundle.getNumberOfReplies().toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(bundle.getLastActivity().toString());
		// TODO: handle reply properly
		ReplyWidget reply1 = ginInjector.createReplyWidget();
		reply1.configure();
		view.addReply(reply1.asWidget());
		ReplyWidget reply2 = ginInjector.createReplyWidget();
		reply2.configure();
		view.addReply(reply2.asWidget());
	}
}
