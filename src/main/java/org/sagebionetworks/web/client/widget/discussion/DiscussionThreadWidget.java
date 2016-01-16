package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadWidget implements DiscussionThreadWidgetView.Presenter{

	DiscussionThreadWidgetView view;
	PortalGinInjector ginInjector;
	GWTWrapper gwtWrapper;
	boolean areRepliesConfigure;

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
			PortalGinInjector ginInjector,
			GWTWrapper gwtWrapper
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.gwtWrapper = gwtWrapper;
		this.areRepliesConfigure = false;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle) {
		view.clear();
		view.setTitle(bundle.getTitle());
		view.setMessage(bundle.getMessageUrl());
		view.setActiveUsers(bundle.getActiveAuthors().toString());
		view.setNumberOfReplies(bundle.getNumberOfReplies().toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(gwtWrapper.getFormattedDateString(bundle.getLastActivity()));
		view.setAuthor(bundle.getCreatedBy());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		if (bundle.getNumberOfReplies() > 0) {
			view.addClickHandlerToShowReplies();
		}
	}

	@Override
	public void toggleThread() {
		view.toggleThread();
	}

	@Override
	public void toggleReplies() {
		if (!areRepliesConfigure) {
			configureReplies();
		}
		view.toggleReplies();
	}

	private void configureReplies() {
		// TODO: handle reply properly
		ReplyWidget reply1 = ginInjector.createReplyWidget();
		reply1.configure();
		view.addReply(reply1.asWidget());
		ReplyWidget reply2 = ginInjector.createReplyWidget();
		reply2.configure();
		view.addReply(reply2.asWidget());

		this.areRepliesConfigure = true;
	}
}
