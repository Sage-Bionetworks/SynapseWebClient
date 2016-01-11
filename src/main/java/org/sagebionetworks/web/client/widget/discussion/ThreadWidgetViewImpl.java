package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ThreadWidgetViewImpl implements ThreadWidgetView {

	public interface Binder extends UiBinder<Widget, ThreadWidgetViewImpl> {}

	@UiField
	Div replyListContainer;

	private Widget widget;
	private ThreadWidget presenter;

	@Inject
	public ThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(ThreadWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addReply(Widget w) {
		replyListContainer.add(w);
	}

	@Override
	public void clear() {
		replyListContainer.clear();
	}
}
