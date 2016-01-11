package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionListWidgetViewImpl implements DiscussionListWidgetView{

	public interface Binder extends UiBinder<Widget, DiscussionListWidgetViewImpl> {}

	@UiField
	Div threadListContainer;

	Widget widget;
	private DiscussionListWidget presenter;

	@Inject
	public DiscussionListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(DiscussionListWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addThread(Widget w) {
		threadListContainer.add(w);
	}

	@Override
	public void clear() {
		threadListContainer.clear();;
	}
}
