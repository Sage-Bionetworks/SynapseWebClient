package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicRowWidgetViewImpl implements TopicRowWidgetView {

	@UiField
	Span subscribeButtonContainer;
	@UiField
	Span topicContainer;

	public interface Binder extends UiBinder<Widget, TopicRowWidgetViewImpl> {
	}

	Widget w;

	@Inject
	public TopicRowWidgetViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	public void setSubscribeButtonWidget(Widget w) {
		subscribeButtonContainer.clear();
		subscribeButtonContainer.add(w);
	};

	public void setTopicWidget(Widget w) {
		topicContainer.clear();
		topicContainer.add(w);
	};
}
