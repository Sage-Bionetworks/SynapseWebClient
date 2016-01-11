package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionListWidget implements DiscussionListWidgetView.Presenter{

	DiscussionListWidgetView view;
	PortalGinInjector ginInjector;

	@Inject
	public DiscussionListWidget(
			DiscussionListWidgetView view,
			PortalGinInjector ginInjector
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
		configure();
	}

	@Override
	public void configure() {
		view.clear();
		ThreadWidget thread1 = ginInjector.getThreadWidget();
		thread1.configure();
		view.addThread(thread1.asWidget());
		ThreadWidget thread2 = ginInjector.getThreadWidget();
		thread2.configure();
		view.addThread(thread2.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
