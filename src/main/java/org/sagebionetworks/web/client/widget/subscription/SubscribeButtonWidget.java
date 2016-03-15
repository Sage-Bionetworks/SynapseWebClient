package org.sagebionetworks.web.client.widget.subscription;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	boolean iconOnly;
	@Inject
	public SubscribeButtonWidget(SubscribeButtonWidgetView view, 
			SubscriptionClientAsync subscribeClient,
			SynapseAlert synAlert,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synAlert = synAlert;
		this.subscribeClient = subscribeClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		iconOnly = false;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public SubscribeButtonWidget showIconOnly() {
		iconOnly = true;
		return this;
	}
	
	public void clear() {
		view.clear();
	}
	
	public void showFollowButton() {
		if (iconOnly) {
			view.showFollowIcon();
		} else {
			view.showFollowButton();
		}
	}
	
	public void showUnfollowButton() {
		if (iconOnly) {
			view.showUnfollowIcon();
		} else {
			view.showUnfollowButton();
		}
	}

	
	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 */
	public void configure(SubscriptionObjectType type, String id) {
		this.id = id;
		this.type = type;
		if (!authController.isLoggedIn()) {
			showFollowButton();
		} else {
			getSubscriptionState();	
		}
	}
	
	/**
	 * @param subscription Can be configured with an existing subscription.  Will not look for subscription, and will render with a way to unsubscribe.
	 */
	public void configure(Subscription subscription) {
		this.id = subscription.getObjectId();
		this.type = subscription.getObjectType();
		this.currentSubscription = subscription;
		showUnfollowButton();
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
					showUnfollowButton();
				} else {
					//not currently subscribed
					showFollowButton();
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
		synAlert.clear();
		//if not logged in, then send to login first
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
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
					showUnfollowButton();
				}
			});
		}
	}
	
	@Override
	public void onUnsubscribe() {
		synAlert.clear();
		view.showLoading();
		subscribeClient.unsubscribe(Long.parseLong(currentSubscription.getSubscriptionId()), new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				view.hideLoading();
				synAlert.handleException(caught);
			};
			@Override
			public void onSuccess(Void result) {
				currentSubscription = null;
				showFollowButton();
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
	
	/**
	 * For testing
	 * @return
	 */
	public boolean isIconOnly(){
		return iconOnly;
	}
	
	public Subscription getCurrentSubscription() {
		return currentSubscription;
	}
}
