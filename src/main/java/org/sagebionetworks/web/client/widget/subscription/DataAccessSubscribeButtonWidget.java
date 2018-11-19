package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.subscription.SortByType;
import org.sagebionetworks.repo.model.subscription.SortDirection;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DataAccessSubscribeButtonWidget implements SubscribeButtonWidgetView.Presenter, SynapseWidgetPresenter, IsWidget {
	
	private SubscribeButtonWidgetView view;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	Subscription currentSubscription;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	Callback onSubscribeCallback, onUnsubscribeCallback;
	ActionMenuWidget.ActionListener subscribeActionListener, unsubscribeActionListener;
	public static final SubscriptionObjectType DATA_ACCESS_SUBMISSION_TYPE = SubscriptionObjectType.DATA_ACCESS_SUBMISSION;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	
	@Inject
	public DataAccessSubscribeButtonWidget(SubscribeButtonWidgetView view, 
			SynapseJavascriptClient jsClient,
			SynapseAlert synAlert,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler) {
		this.view = view;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
		subscribeActionListener = action -> {
			onSubscribe();
		};
		unsubscribeActionListener = action -> {
			onUnsubscribe();
		};
	}
	
	public void clear() {
		view.clear();
	}
	
	public void showFollowButton() {
		view.showFollowButton();
	}
	
	public void showUnfollowButton() {
		view.showUnfollowButton();
	}
	
	public void setOnSubscribeCallback(Callback c) {
		onSubscribeCallback = c;
	}
	
	public void setOnUnsubscribeCallback(Callback c) {
		onUnsubscribeCallback = c;
	}
	
	public void configure() {
		view.setVisible(false);
		isACTMemberAsyncHandler.isACTMember(isACT -> {
			if (isACT) {
				view.setVisible(true);
				if (!authController.isLoggedIn()) {
					showFollowButton();
				} else {
					getSubscriptionState();	
				}
			}
		});
	}
	
	public void getSubscriptionState() {
		view.clear();
		synAlert.clear();
		// attempt to find subscription for data access type
		SubscriptionRequest request = new SubscriptionRequest();
		request.setObjectType(DATA_ACCESS_SUBMISSION_TYPE);
		jsClient.getAllSubscriptions(DATA_ACCESS_SUBMISSION_TYPE, 1L, 0L, SortByType.CREATED_ON, SortDirection.DESC, new AsyncCallback<SubscriptionPagedResults>() {
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
			jsClient.subscribeToAll(DATA_ACCESS_SUBMISSION_TYPE, new AsyncCallback<Subscription>() {
				public void onFailure(Throwable caught) {
					view.hideLoading();
					synAlert.handleException(caught);
				};
				@Override
				public void onSuccess(Subscription result) {
					//success
					currentSubscription = result;
					showUnfollowButton();
					if (onSubscribeCallback != null) {
						onSubscribeCallback.invoke();
					}
				}
			});
		}
	}
	
	@Override
	public void onUnsubscribe() {
		synAlert.clear();
		view.showLoading();
		jsClient.unsubscribe(currentSubscription.getSubscriptionId(), new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				view.hideLoading();
				synAlert.handleException(caught);
			};
			@Override
			public void onSuccess(Void result) {
				currentSubscription = null;
				showFollowButton();
				if (onUnsubscribeCallback != null) {
					onUnsubscribeCallback.invoke();
				}
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
	
	public Subscription getCurrentSubscription() {
		return currentSubscription;
	}
	
	public void setButtonSize(ButtonSize size) {
		view.setButtonSize(size);
	}
}
