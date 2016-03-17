package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionListWidget implements SubscriptionListWidgetView.Presenter, SynapseWidgetPresenter {
	
	private SubscriptionListWidgetView view;
	SubscriptionClientAsync subscribeClient;
	SynapseAlert synAlert;
	SubscriptionObjectType filter;
	PortalGinInjector ginInjector;
	AuthenticationController authController;
	private static final Long LIMIT = 20L;
	private Long currentOffset;
	
	@Inject
	public SubscriptionListWidget(SubscriptionListWidgetView view, 
			SubscriptionClientAsync subscribeClient,
			PortalGinInjector ginInjector,
			SynapseAlert synAlert,
			AuthenticationController authController) {
		this.view = view;
		this.synAlert = synAlert;
		this.subscribeClient = subscribeClient;
		this.authController = authController;
		this.ginInjector = ginInjector;
		
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void configure() {
		view.clearFilter();
		reloadSubscriptions();
	}
	
	private void reloadSubscriptions() {
		currentOffset = 0L;
		view.clearSubscriptions();
		if (authController.isLoggedIn()) {
			getMoreSubscriptions();	
		}
	}
	
	private void getMoreSubscriptions() {
		synAlert.clear();
		subscribeClient.getAllSubscriptions(filter, LIMIT, currentOffset, new AsyncCallback<SubscriptionPagedResults>() {
			@Override
			public void onSuccess(SubscriptionPagedResults results) {
				boolean isMore = results.getTotalNumberOfResults() > currentOffset + results.getResults().size();
				view.setMoreButtonVisible(isMore);
				currentOffset += LIMIT;
				//for each subscription, add a row.
				GWT.debugger();
				for (Subscription subscription : results.getResults()) {
					SubscribeButtonWidget subscribeButton = ginInjector.getSubscribeButtonWidget();
					subscribeButton.configure(subscription);
					TopicWidget topicWidget = ginInjector.getTopicWidget();
					topicWidget.configure(subscription.getObjectType(), subscription.getObjectId());
					view.addNewSubscription(subscribeButton.asWidget(), topicWidget.asWidget());
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onFilter(SubscriptionObjectType type) {
		filter = type;
		reloadSubscriptions();
	}
	
	@Override
	public void onMore() {
		getMoreSubscriptions();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
