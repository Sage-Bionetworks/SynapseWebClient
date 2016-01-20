package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;
	GWTWrapper gwtWrapper;
	UserBadge authorWidget;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			UserBadge authorWidget,
			GWTWrapper gwtWrapper
			) {
		this.view = view;
		this.authorWidget = authorWidget;
		this.gwtWrapper = gwtWrapper;
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
	}

	public void configure(DiscussionReplyBundle bundle) {
		view.clear();
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		view.setMessage("reply message");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
