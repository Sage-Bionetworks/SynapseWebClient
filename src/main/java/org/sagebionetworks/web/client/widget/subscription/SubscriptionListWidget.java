package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

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
	private static final Long LIMIT = 10L;
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
		view.setNoItemsMessageVisible(true);
		if (authController.isLoggedIn()) {
			getMoreSubscriptions();	
		}
	}
	
	private void getMoreSubscriptions() {
		synAlert.clear();
		subscribeClient.getAllSubscriptions(filter, LIMIT, currentOffset, new AsyncCallback<SubscriptionPagedResults>() {
			@Override
			public void onSuccess(SubscriptionPagedResults results) {
				int currentResultSize = results.getResults().size();
				if (currentResultSize > 0) {
					view.setNoItemsMessageVisible(false);	
				}
				view.setMoreButtonVisible(currentResultSize == LIMIT );
				currentOffset += LIMIT;
				//for each subscription, add a row.
				for (Subscription subscription : results.getResults()) {
					TopicRowWidget topicRow = ginInjector.getTopicRowWidget();
					topicRow.configure(subscription);
					view.addNewSubscription(topicRow.asWidget());
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
