package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicRowWidget implements TopicRowWidgetView.Presenter, SynapseWidgetPresenter {
	
	private TopicRowWidgetView view;
	TopicWidget topic;
	SubscribeButtonWidget subscribeButton;
	DateTimeUtils dateTimeUtils;
	
	@Inject
	public TopicRowWidget(TopicRowWidgetView view, 
			TopicWidget topic,
			SubscribeButtonWidget subscribeButton,
			DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.topic = topic;
		this.subscribeButton = subscribeButton;
		this.dateTimeUtils = dateTimeUtils;
		view.setSubscribeButtonWidget(subscribeButton.asWidget());
		view.setTopicWidget(topic.asWidget());
		view.setPresenter(this);
		subscribeButton.setButtonSize(ButtonSize.EXTRA_SMALL);
	}
	
	public void configure(Subscription subscription) {
		subscribeButton.configure(subscription);
		topic.configure(subscription.getObjectType(), subscription.getObjectId());
		view.setPostedOn(dateTimeUtils.getDateString(subscription.getCreatedOn()));
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
