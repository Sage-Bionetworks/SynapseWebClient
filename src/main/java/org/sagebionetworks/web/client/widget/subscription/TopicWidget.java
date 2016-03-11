package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicWidget implements TopicWidgetView.Presenter, SynapseWidgetPresenter {
	
	private TopicWidgetView view;
	SubscriptionClientAsync subscriptionClient;
	
	@Inject
	public TopicWidget(TopicWidgetView view, SubscriptionClientAsync subscriptionClient) {
		this.view = view;
		this.subscriptionClient = subscriptionClient;
		view.setPresenter(this);
	}
	
	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 */
	public void configure(SubscriptionObjectType type, String id) {
		
	}
	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
