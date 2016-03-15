package org.sagebionetworks.web.client.widget.subscription;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscribeButtonWidget implements SubscribeButtonWidgetView.Presenter, SynapseWidgetPresenter {
	
	private SubscribeButtonWidgetView view;
	SubscriptionClientAsync subscribeClient;
	SynapseAlert synAlert;
	SubscriptionObjectType type;
	String id;
	Subscription currentSubscription;
	
	@Inject
	public SubscribeButtonWidget(SubscribeButtonWidgetView view, 
			SubscriptionClientAsync subscribeClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.synAlert = synAlert;
		this.subscribeClient = subscribeClient;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	
	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 */
	public void configure(SubscriptionObjectType type, String id) {
		this.id = id;
		this.type = type;
		getSubscriptionState();
	}
	
	public void getSubscriptionState() {
		view.clear();
		synAlert.clear();
		SubscriptionRequest request = new SubscriptionRequest();
		request.setObjectType(type);
		List<String> idList = new ArrayList<String>();
		idList.add(id);
		request.setIdList(idList);
		subscribeClient.listSubscription(request, new AsyncCallback<SubscriptionPagedResults>() {
			@Override
			public void onSuccess(SubscriptionPagedResults results) {
				if (results.getTotalNumberOfResults() > 0) {
					//currently subscribed.
					currentSubscription = results.getResults().get(0);
					view.setSubscribed();
				} else {
					//not currently subscribed
					view.setUnsubscribed();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onSubscribe() {
		view.showLoading();
		Topic topic = new Topic();
		topic.setObjectId(id);
		topic.setObjectType(type);
		subscribeClient.subscribe(topic, new AsyncCallback<Subscription>() {
			public void onFailure(Throwable caught) {
				view.hideLoading();
				synAlert.handleException(caught);
			};
			@Override
			public void onSuccess(Subscription result) {
				//success
				currentSubscription = result;
				view.setSubscribed();
			}
		});
	}
	
	@Override
	public void onUnsubscribe() {
		view.showLoading();
		subscribeClient.unsubscribe(Long.parseLong(currentSubscription.getSubscriptionId()), new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				view.hideLoading();
				synAlert.handleException(caught);
			};
			@Override
			public void onSuccess(Void result) {
				currentSubscription = null;
				view.setUnsubscribed();
			}
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
