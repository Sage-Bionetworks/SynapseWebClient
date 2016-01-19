package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;
	GWTWrapper gwtWrapper;
	PortalGinInjector ginInjector;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			GWTWrapper gwtWrapper,
			PortalGinInjector ginInjector
			) {
		this.view = view;
		this.gwtWrapper = gwtWrapper;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
	}

	public void configure(DiscussionReplyBundle bundle) {
		view.clear();
		UserBadge author = ginInjector.getUserBadgeWidget();
		author.configure(bundle.getCreatedBy());
		view.setAuthor(author.asWidget());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		view.setMessage("reply message");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
