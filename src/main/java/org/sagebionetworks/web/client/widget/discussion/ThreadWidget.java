package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ThreadWidget implements ThreadWidgetView.Presenter{

	ThreadWidgetView view;
	PortalGinInjector ginInjector;

	@Inject
	public ThreadWidget(
			ThreadWidgetView view,
			PortalGinInjector ginInjector
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		view.setPresenter(this);
		configure();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure() {
		view.clear();
		ReplyWidget reply1 = ginInjector.createReplyWidget();
		reply1.configure();
		view.addReply(reply1.asWidget());
		ReplyWidget reply2 = ginInjector.createReplyWidget();
		reply2.configure();
		view.addReply(reply2.asWidget());
	}
}
