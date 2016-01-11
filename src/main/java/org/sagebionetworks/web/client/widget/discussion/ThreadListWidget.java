package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ThreadListWidget implements ThreadListWidgetView.Presenter{

	ThreadListWidgetView view;
	PortalGinInjector ginInjector;

	@Inject
	public ThreadListWidget(
			ThreadListWidgetView view,
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
		ThreadWidget thread1 = ginInjector.createThreadWidget();
		thread1.configure();
		view.addThread(thread1.asWidget());
		ThreadWidget thread2 = ginInjector.createThreadWidget();
		thread2.configure();
		view.addThread(thread2.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
