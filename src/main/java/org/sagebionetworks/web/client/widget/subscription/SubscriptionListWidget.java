package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionListWidget implements SubscriptionListWidgetView.Presenter, SynapseWidgetPresenter, PageChangeListener {
	
	private SubscriptionListWidgetView view;
	SubscriptionClientAsync subscribeClient;
	SynapseAlert synAlert;
	SubscriptionObjectType filter;
	PortalGinInjector ginInjector;
	AuthenticationController authController;
	public static final Long LIMIT = 10L;
	DetailedPaginationWidget paginationWidget;
	
	@Inject
	public SubscriptionListWidget(SubscriptionListWidgetView view, 
			SubscriptionClientAsync subscribeClient,
			PortalGinInjector ginInjector,
			SynapseAlert synAlert,
			AuthenticationController authController,
			DetailedPaginationWidget paginationWidget) {
		this.view = view;
		this.synAlert = synAlert;
		this.subscribeClient = subscribeClient;
		this.authController = authController;
		this.ginInjector = ginInjector;
		this.paginationWidget = paginationWidget;
		
		view.setSynAlert(synAlert.asWidget());
		view.setPagination(paginationWidget.asWidget());
		view.setPresenter(this);
	}
	
	public void configure() {
		filter = SubscriptionObjectType.FORUM;
		view.clearFilter();
		view.clearSubscriptions();
		if (authController.isLoggedIn()) {
			onPageChange(0L);
		}
	}
	
	@Override
	public void onPageChange(final Long newOffset) {
		synAlert.clear();
		view.clearSubscriptions();
		view.setNoItemsMessageVisible(false);
		view.setLoadingVisible(true);
		subscribeClient.getAllSubscriptions(filter, LIMIT, newOffset, new AsyncCallback<SubscriptionPagedResults>() {
			@Override
			public void onSuccess(SubscriptionPagedResults results) {
				view.setLoadingVisible(false);
				view.setNoItemsMessageVisible(results.getTotalNumberOfResults() == 0);
				paginationWidget.configure(LIMIT, newOffset, results.getTotalNumberOfResults(), SubscriptionListWidget.this);
				//for each subscription, add a row.
				for (Subscription subscription : results.getResults()) {
					TopicRowWidget topicRow = ginInjector.getTopicRowWidget();
					topicRow.configure(subscription);
					view.addNewSubscription(topicRow.asWidget());
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onFilter(SubscriptionObjectType type) {
		filter = type;
		onPageChange(0L);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
