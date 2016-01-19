package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.GWTWrapper;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;
	GWTWrapper gwtWrapper;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			GWTWrapper gwtWrapper
			) {
		this.view = view;
		this.gwtWrapper = gwtWrapper;
		view.setPresenter(this);
	}

	public void configure(DiscussionReplyBundle bundle) {
		view.clear();
		view.setAuthor(bundle.getCreatedBy());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		view.setMessage("reply message");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
